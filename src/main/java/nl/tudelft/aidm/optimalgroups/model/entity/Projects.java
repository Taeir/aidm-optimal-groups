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

	class InDb implements Projects
	{
		private DataSource dataSource;
		private int courseEditionId;

		private List<Project> projectList = null;

		InDb(DataSource dataSource, int courseEditionId)
		{
			this.dataSource = dataSource;
			this.courseEditionId = courseEditionId;
		}

		public int count()
		{
			return projectList().size();
		}

		protected List<Project> projectList()
		{
			if (projectList == null)
			{
				projectList = fetchFromDb();
			}

			return projectList;
		}

//		public Project withIndex(int idx)
//		{
//			return projectList.get(idx);
//		}

		private int numTotalSlots = -1;

		@Override
		public int countAllSlots()
		{
			// lazy eval
			if (numTotalSlots < 0)
			{
				numTotalSlots = projectList.stream()
					.map(project -> project.numSlots)
					.mapToInt(Integer::intValue)
					.sum();
			}

			return numTotalSlots;
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
