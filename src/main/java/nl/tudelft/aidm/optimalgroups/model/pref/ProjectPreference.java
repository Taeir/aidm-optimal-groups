package nl.tudelft.aidm.optimalgroups.model.pref;

import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.List;

public interface ProjectPreference
{
	// TODO: determine representation (let algo guide this choice)
	int[] asArray();

	class fromDb implements ProjectPreference
	{
		private final DataSource dataSource;
		private final String userId;
		private final String courseEditionId;

		private int[] preferences = null;

		public fromDb(DataSource dataSource, String userId, String courseEditionId)
		{
			this.dataSource = dataSource;
			this.userId = userId;
			this.courseEditionId = courseEditionId;
		}

		@Override
		public int[] asArray()
		{
			if (preferences == null)
			{
				preferences = fetchFromDb().stream()
					.mapToInt(Integer::intValue)
					.toArray();
			}

			return preferences;
		}

		private List<Integer> fetchFromDb()
		{
			Sql2o sql2o = new Sql2o(dataSource);
			try (var conn = sql2o.open())
			{
				final var sql = "SELECT project_id " +
					"FROM project_preferences " +
					"WHERE user_id = :userId AND course_edition_id = :courseEditionId " +
					"ORDER BY priority";

				Query query = conn.createQuery(sql)
					.addParameter("userId", userId)
					.addParameter("courseEditionId", courseEditionId);

				List<Integer> prefs = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("project_id"));
				return prefs;
			}
		}
	}
}
