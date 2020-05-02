package nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectPreferencesInDb implements ProjectPreference
{
	private final DataSource dataSource;
	private final Integer userId;
	private final CourseEdition courseEdition;

	private Integer[] preferences = null;
	private List<Project> preferencesAsProjectList = null;
	private Map<Integer, Integer> preferencesMap = null;

	private Boolean isIndifferent = null;

	public ProjectPreferencesInDb(DataSource dataSource, Integer userId, CourseEdition courseEdition)
	{
		this.dataSource = dataSource;
		this.userId = userId;
		this.courseEdition = courseEdition;
	}

	@Override
	public boolean isCompletelyIndifferent()
	{
		if (isIndifferent == null) {
			// force retrieval
			asArray();
		}

		return isIndifferent;
	}

	@Override
	public Integer[] asArray()
	{
		if (preferences == null)
		{
			var submittedPreferencesOfAgentAsProjectIds = fetchFromDb().stream()
				.mapToInt(Integer::intValue)
				.toArray();

			var projectsInSubmittedPreferences = Projects.from(
				Arrays.stream(submittedPreferencesOfAgentAsProjectIds).boxed()
					.map(Project.ProjectsWithDefaultSlotAmount::new).collect(Collectors.toList())
			);

			var allProjects = courseEdition.allProjects();
			var projectsNotInPreferences = allProjects.without(projectsInSubmittedPreferences);

			ArrayList<Project> shuffledMissingProjects = new ArrayList<>(projectsNotInPreferences.asCollection());
			Collections.shuffle(shuffledMissingProjects);

			// Join the two lists/arrays using streams into a single array - Java has no native Array.join
			preferences = Stream.concat(
					Arrays.stream(submittedPreferencesOfAgentAsProjectIds).boxed(),
					shuffledMissingProjects.stream().map(Project::id)
				)
				.toArray(Integer[]::new);

			// Set indifference flag by inspecting its submitted preferences
			isIndifferent = submittedPreferencesOfAgentAsProjectIds.length == 0;
		}

		return preferences;
	}

	@Override
	public synchronized List<Project> asListOfProjects()
	{
		if (preferencesAsProjectList == null) {
			var projectIdsInOrder = asArray();

			var allProjects = courseEdition.allProjects();
			var projectList = new ArrayList<Project>(projectIdsInOrder.length);

			for (var projId : projectIdsInOrder) {
				projectList.add(allProjects.findWithId(projId).get());
			}

			preferencesAsProjectList = Collections.unmodifiableList(projectList);

		}

		return preferencesAsProjectList;
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
				.addParameter("courseEditionId", courseEdition.bepSysId());

			List<Integer> prefs = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("project_id"));
			return prefs;
		}
	}
}
