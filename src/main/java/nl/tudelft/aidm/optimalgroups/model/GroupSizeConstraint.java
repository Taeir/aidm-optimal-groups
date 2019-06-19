package nl.tudelft.aidm.optimalgroups.model;

import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.List;

public interface GroupSizeConstraint
{
	int minSize();
	int maxSize();

	class fromDb implements GroupSizeConstraint
	{
		private final DataSource dataSource;
		private final int courseEditionId;

		private int minSize = -1;
		private int maxSize = -1;

		public fromDb(DataSource dataSource, int courseEditionId)
		{
			this.dataSource = dataSource;
			this.courseEditionId = courseEditionId;
		}

		@Override
		public int minSize()
		{
			if (minSize == -1)
				fetchValuesFromDb();

			return minSize;
		}

		@Override
		public int maxSize()
		{
			if (maxSize == -1)
				fetchValuesFromDb();

			return maxSize;
		}

		private void fetchValuesFromDb()
		{
			int[] groupSizes = new int[2];
			var sql = "SELECT min_group_size FROM course_configurations where course_edition_id = " + courseEditionId;
			var sql2 = "SELECT max_group_size FROM course_configurations where course_edition_id = " + courseEditionId;
			try (var connection = new Sql2o(dataSource).open())
			{
				Query query = connection.createQuery(sql);
				List<Integer> minGroupSizes = query.executeAndFetch(
					(ResultSetHandler<Integer>) rs ->
						(rs.getInt("min_group_size"))
				);
				Query query2 = connection.createQuery(sql2);
				List<Integer> maxGroupSizes = query2.executeAndFetch(
					(ResultSetHandler<Integer>) rs ->
						(rs.getInt("max_group_size"))
				);
				minSize = minGroupSizes.get(0);
				maxSize = maxGroupSizes.get(0);
			}
		}
	}
}
