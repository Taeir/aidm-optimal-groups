package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface GroupPreference
{
	// TODO: determine representation (let algo guide this choice)
	// for now as int array purely for memory efficiency
	int[] asArray();

	List<Agent> asList();

	class fromDb implements GroupPreference
	{
		private DataSource dataSource;
		private Integer userId;
		private Integer courseEditionId;

		private int[] preference = null;

		public fromDb(DataSource dataSource, Integer userId, Integer courseEditionId)
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

		private List<Agent> asList = null;
		@Override
		public List<Agent> asList()
		{
			if (asList == null) {
				asList = Arrays.stream(asArray())
					.boxed()
					.map(friendAgentId -> Agent.AgentInBepSysSchemaDb.from(dataSource, friendAgentId, courseEditionId))
					.collect(Collectors.toList());
			}

			return asList;
		}

		@Override
		public String toString()
		{
			return "group pref: " + Arrays.toString(asArray());
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
