package nl.tudelft.aidm.optimalgroups.model.project;

import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProjectsInDb extends ListBasedProjects
{
	private DataSource dataSource;
	private int courseEditionId;

	private List<Project> projectList = null;
	private Map<Integer, Project> byId = null;

	ProjectsInDb(DataSource dataSource, int courseEditionId)
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
	public Optional<Project> findWithId(int projectId)
	{
		if (byId == null) {
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

}
