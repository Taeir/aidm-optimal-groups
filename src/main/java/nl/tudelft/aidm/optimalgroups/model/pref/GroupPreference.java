package nl.tudelft.aidm.optimalgroups.model.pref;

import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.List;

public interface GroupPreference
{
	// TODO: determine representation (let algo guide this choice)
	// for now as int array purely for memory efficiency
	int[] asArray();

	class fromDb implements GroupPreference
	{
		private DataSource dataSource;
		private String userId;
		private String courseEditionId;

		private int[] preference = null;

		public fromDb(DataSource dataSource, String userId, String courseEditionId)
		{
			this.dataSource = dataSource;
			this.userId = userId;
			this.courseEditionId = courseEditionId;
		}

		@Override
		public int[] asArray()
		{
			if (preference == null)
			{
				preference = fetchFromDb().stream()
					.mapToInt(Integer::intValue)
					.toArray();
			}

			return preference;
		}

		private List<Integer> fetchFromDb()
		{
			final var sql = "SELECT student_id " +
				"FROM student_preferences " +
				"WHERE user_id = :userId AND course_edition_id = :courseEditionId";

			var sql2o = new Sql2o(dataSource);
			try (var conn = sql2o.open())
			{
				Query query = conn.createQuery(sql)
					.addParameter("userId", userId)
					.addParameter("courseEditionId", courseEditionId);

				List<Integer> preferredStudents = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("student_id"));
				return preferredStudents;
			}
		}
	}
}
