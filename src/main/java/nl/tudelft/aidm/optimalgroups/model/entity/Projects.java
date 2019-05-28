package nl.tudelft.aidm.optimalgroups.model.entity;

import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.List;

public interface Projects
{
	int count();
	int countAllSlots();
//	Project withIndex();

	List<Project.ProjectSlot> slotsForProject(int projectId);

	abstract class ListBasedProjects implements Projects
	{
		public int count()
		{
			return projectList().size();
		}

		abstract protected List<Project> projectList();

		public List<Project.ProjectSlot> slotsForProject(int projectId) {
			String projectName = String.valueOf(projectId);
			Project project = this.projectList().stream()
				.filter(p -> p.name().equals(projectName))
				.findAny().get();

			return project.slots();

		}

		private int numTotalSlots = -1;

		@Override
		public int countAllSlots()
		{
			// lazy eval
			if (numTotalSlots < 0)
			{
				numTotalSlots = projectList().stream()
					.map(project -> project.slots().size())
					.mapToInt(Integer::intValue)
					.sum();
			}

			return numTotalSlots;
		}
	}

	class InDb extends ListBasedProjects
	{
		private DataSource dataSource;
		private int courseEditionId;

		private List<Project> projectList = null;

		InDb(DataSource dataSource, int courseEditionId)
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

		private List<Project> fetchFromDb()
		{
			var sql = "SELECT id FROM test.projects where course_edition_id = :courseEditionId";
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

	public static Projects fromDb(DataSource dataSource, int courseEditionInt)
	{
		return new InDb(dataSource, courseEditionInt);
	}
}
