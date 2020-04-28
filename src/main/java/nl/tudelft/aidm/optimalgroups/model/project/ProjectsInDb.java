package nl.tudelft.aidm.optimalgroups.model.project;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import plouchtch.assertion.Assert;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProjectsInDb extends ListBasedProjects
{
	private DataSource dataSource;
	private Integer courseEditionId;

	private List<Project> projectList = null;
	private Map<Integer, Project> byId = null;

	ProjectsInDb(DataSource dataSource, Integer courseEditionId)
	{
		this.dataSource = dataSource;
		this.courseEditionId = courseEditionId;
	}



	@Override
	protected List<Project> projectList()
	{
		if (projectList == null)
		{
			projectList = fetchFromDb();
		}

		return projectList;
	}


	@Override
	public synchronized Optional<Project> findWithId(int projectId)
	{
		if (byId == null) {
			byId = new HashMap<>();
			projectList().forEach(project -> {
				byId.put(project.id(), project);
			});
		}

		Project value = byId.get(projectId);
		return Optional.ofNullable(value);
	}

	private List<Project> fetchFromDb()
	{
		var sql = "SELECT id FROM projects where course_edition_id = :courseEditionId";
		try (var connection = new Sql2o(dataSource).open())
		{
			Query query = connection.createQuery(sql);
			query.addParameter("courseEditionId", courseEditionId);

			List<Project> projectsAsList = query.executeAndFetch(
				(ResultSetHandler<Project>) rs ->
					new Project.withDefaultSlots(rs.getInt("id"))
			);

			return projectsAsList;
		}
	}


	/* FACTORY METHOD / CACHE */

	private static DataSource lastUsedDataSource = null;
	private static final Map<Integer, ProjectsInDb> projectsCache = new HashMap<>();

	public static ProjectsInDb possibleCached(DataSource dataSource, Integer courseEditionId)
	{
		if (lastUsedDataSource == null) {
			lastUsedDataSource = dataSource;
		}

		Assert.that(lastUsedDataSource == dataSource)
			.orThrow(RuntimeException.class, "Projects are cached for a different datasource! Please fix the cache impl to support this use case.");

		if (projectsCache.containsKey(courseEditionId) == false) {
			projectsCache.put(courseEditionId, new ProjectsInDb(dataSource, courseEditionId));
		}

		var proj = projectsCache.get(courseEditionId);
		return proj;
	}

}
