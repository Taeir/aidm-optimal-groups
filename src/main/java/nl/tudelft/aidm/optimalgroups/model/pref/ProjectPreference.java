package nl.tudelft.aidm.optimalgroups.model.pref;

import edu.princeton.cs.algs4.StdOut;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ProjectPreference
{
	// TODO: determine representation (let algo guide this choice)
	int[] asArray();

	/**
	 * Return the preferences as a map, where the keys represent the project
	 * and the value represents the rank of the project.
	 *
	 * The highest rank is 1 and represents the most preferable project.
	 *
	 * @return Map
	 */
	Map<Integer, Integer> asMap();

	default void forEach(ProjectPreferenceConsumer iter)
	{
		int[] prefArray = asArray();
		for (int i = 0; i < prefArray.length; i++)
		{
			iter.apply(prefArray[i], i+1);
		}
	}

	default int differenceTo(ProjectPreference otherPreference) {
		Map<Integer, Integer> own = asMap();
		Map<Integer, Integer> other = otherPreference.asMap();

		// If the other does not have any preferences, return maximum difference to
		// avoid picking this matching over people that do have preferences
		if (other.size() == 0 || own.size() == 0) {
			return Integer.MAX_VALUE;
		}

		int difference = 0;
		for (Map.Entry<Integer, Integer> entry : own.entrySet()) {
			difference += Math.abs(entry.getValue() - other.get(entry.getKey()));
		}

		return difference;
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
		private Map<Integer, Integer> preferencesMap = null;

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

		@Override
		public Map<Integer, Integer> asMap() {
			if (this.preferencesMap == null) {


				int[] preferences = this.asArray();
				this.preferencesMap = new HashMap<>(preferences.length);

				for (int rank = 1; rank <= preferences.length; rank++) {
					int project = preferences[rank - 1];
					this.preferencesMap.put(project, rank);
				}
			}

			return this.preferencesMap;
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
