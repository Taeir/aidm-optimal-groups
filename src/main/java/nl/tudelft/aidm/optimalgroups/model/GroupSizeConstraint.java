package nl.tudelft.aidm.optimalgroups.model;

import nl.tudelft.aidm.optimalgroups.model.dataset.CourseEdition;
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
		private final CourseEdition courseEdition;

		private int minSize = -1;
		private int maxSize = -1;

		public fromDb(DataSource dataSource, CourseEdition courseEdition)
		{
			this.dataSource = dataSource;
			this.courseEdition = courseEdition;
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

		@Override
		public String toString()
		{
			return String.format("gsc [%s,%s]", minSize(), maxSize());
		}

		private void fetchValuesFromDb()
		{
			var sql_min = "SELECT min_group_size FROM course_configurations where course_edition_id = " + courseEdition.bepSysId();
			var sql_max = "SELECT max_group_size FROM course_configurations where course_edition_id = " + courseEdition.bepSysId();

			try (var connection = new Sql2o(dataSource).open())
			{
				Query queryMin = connection.createQuery(sql_min);
				List<Integer> minGroupSizes = queryMin.executeAndFetch(
					(ResultSetHandler<Integer>) rs ->
						(rs.getInt("min_group_size"))
				);
				minSize = minGroupSizes.get(0);

				Query queryMax = connection.createQuery(sql_max);
				List<Integer> maxGroupSizes = queryMax.executeAndFetch(
					(ResultSetHandler<Integer>) rs ->
						(rs.getInt("max_group_size"))
				);

				maxSize = maxGroupSizes.get(0);
			}
		}
	}
}
