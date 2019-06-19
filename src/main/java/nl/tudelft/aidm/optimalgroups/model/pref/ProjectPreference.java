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

	default void forEach(ProjectPreferenceConsumer iter)
	{
		int[] prefArray = asArray();
		for (int i = 0; i < prefArray.length; i++)
		{
			iter.apply(prefArray[i], i+1);
		}
	}

	/**
	 * Checks if the project preferences indicate complete indifference, that is an absence of preference.
	 * In case of BepSys: the agent has no preferences available. In other scenarios this might mean that the
	 * the available choices have equal rank to the agent
	 * @return
	 */
	default boolean isCompletelyIndifferent() {
		return asArray().length == 0;
	}

	interface ProjectPreferenceConsumer
	{
		/**
		 * @param projectId the id of the project that has the given rank
		 * @param rank Rank of the preference, 1 being highest
		 */
		void apply(int projectId, int rank);
	}

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
				final var sql = "SELECT distinct(project_id) " +
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
