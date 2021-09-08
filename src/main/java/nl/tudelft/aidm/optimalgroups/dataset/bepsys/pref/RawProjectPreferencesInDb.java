package nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.pref.base.AbstractListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import plouchtch.lang.Lazy;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RawProjectPreferencesInDb extends AbstractListBasedProjectPreferences
{
	private final DataSource dataSource;
	private final Integer bepSysUserId;
	private final CourseEdition courseEdition;

	private Project[] asArray = null;
	private List<Project> asList = null;
	
	private Lazy<Agent> owner;

	public RawProjectPreferencesInDb(DataSource dataSource, Integer bepSysUserId, CourseEdition courseEdition)
	{
		this.dataSource = dataSource;
		this.bepSysUserId = bepSysUserId;
		this.courseEdition = courseEdition;
		
		this.owner = new Lazy<>(() -> courseEdition.findAgentByUserId(bepSysUserId).orElseThrow());
	}

	@Override
	public Object owner()
	{
		return owner.get();
	}

	@Override
	public synchronized Project[] asArray()
	{
		if (asArray == null)
		{
			asArray = asList().toArray(Project[]::new);
		}

		return asArray;
	}

	@Override
	public synchronized List<Project> asList()
	{
		if (asList == null)
		{
			Function<Integer, Project> findProjectWithBepSysId = bepSysId -> courseEdition.allProjects().asCollection().stream()
					.filter(project -> ((Project.BepSysProject) project).bepsysId == bepSysId)
					.findAny().orElseThrow();
			
			asList = fetchFromDb().stream()
					.map(findProjectWithBepSysId)
					.toList();
		}

		return asList;
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
					.addParameter("userId", bepSysUserId)
					.addParameter("courseEditionId", courseEdition.bepSysId());

			List<Integer> prefs = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("project_id"));
			return prefs;
		}
	}
}
