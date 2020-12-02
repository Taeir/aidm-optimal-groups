package nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.rank.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.base.AbstractListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RawProjectPreferencesInDb extends AbstractListBasedProjectPreferences
{
	private final DataSource dataSource;
	private final Integer userId;
	private final CourseEdition courseEdition;

	private Integer[] preferences = null;
	private List<Project> preferencesAsProjectList = null;


	// Indicates the rank (inclusive) from which the agent is indifferent
	// so all preferences of that rank and higher have the same rank, that
	// of the value of the field.
	private Integer isIndifferentFromRank = 0;

	public RawProjectPreferencesInDb(DataSource dataSource, Integer userId, CourseEdition courseEdition)
	{
		this.dataSource = dataSource;
		this.userId = userId;
		this.courseEdition = courseEdition;
	}

	@Override
	public synchronized Integer[] asArray()
	{
		if (preferences == null)
		{
			var asList = asListOfProjects();
			preferences = asList.stream()
				.map(Project::id)
				.toArray(Integer[]::new);
		}

		return preferences;
	}

	@Override
	public synchronized List<Project> asListOfProjects()
	{
		if (preferencesAsProjectList == null) {
			preferencesAsProjectList = fetchFromDb().stream()
				.map(id -> courseEdition.allProjects().findWithId(id).orElseThrow())
				.collect(Collectors.toUnmodifiableList());
		}

		return preferencesAsProjectList;
	}

	@Override
	public String toString() {
		return "proj pref: " + Arrays.toString(asArray());
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
