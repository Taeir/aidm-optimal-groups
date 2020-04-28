package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.tudelft.aidm.optimalgroups.Application.courseEdition;

public class ProjectPreferencesInDb implements ProjectPreference
{
	private final DataSource dataSource;
	private final Integer userId;
	private final Integer courseEditionId;

	private Integer[] preferences = null;
	private Map<Integer, Integer> preferencesMap = null;

	public ProjectPreferencesInDb(DataSource dataSource, Integer userId, Integer courseEditionId)
	{
		this.dataSource = dataSource;
		this.userId = userId;
		this.courseEditionId = courseEditionId;
	}

	@Override
	public Integer[] asArray()
	{
		if (preferences == null)
		{
			var preferencesOfAgent = fetchFromDb().stream()
				.mapToInt(Integer::intValue)
				.toArray();

			var projectsInPreferences = Projects.from(
				Arrays.stream(preferencesOfAgent).boxed()
					.map(Project.withDefaultSlots::new).collect(Collectors.toList())
			);

			var allProjects = Projects.fromDb(dataSource, courseEditionId);

			var projectsNotInPreferences = allProjects.without(projectsInPreferences);

			ArrayList<Project> shuffledMissingProjects = new ArrayList<>(projectsNotInPreferences.asCollection());
			Collections.shuffle(shuffledMissingProjects);

			// Join the two lists/arrays with streams into a single array - Java has not native Array.join
			preferences = Stream.concat(
					Arrays.stream(preferencesOfAgent).boxed(),
					shuffledMissingProjects.stream().map(Project::id)
				)
				.toArray(Integer[]::new);

		}

		return preferences;
	}

	@Override
	public String toString() {
		return "proj pref: " + Arrays.toString(asArray());
	}

	@Override
	public Map<Integer, Integer> asMap() {
		if (this.preferencesMap == null) {

			Integer[] preferences = this.asArray();
			this.preferencesMap = new HashMap<>(preferences.length);

			for (int rank = 1; rank <= preferences.length; rank++) {
				int project = preferences[rank - 1];
				this.preferencesMap.put(project, rank);
			}
		}

		return this.preferencesMap;
	}

	private List<Integer> fetchFromDb()
	{
		Sql2o sql2o = new Sql2o(dataSource);
		try (var conn = sql2o.open())
		{
			final var sql = "SELECT distinct(project_id) " +
				"FROM project_preferences " +
				"WHERE user_id = :userId AND course_edition_id = :courseEditionId " +
				"ORDER BY priority";

			Query query = conn.createQuery(sql)
				.addParameter("userId", userId)
				.addParameter("courseEditionId", courseEditionId);

			List<Integer> prefs = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("project_id"));
			return prefs;
		}
	}
}
