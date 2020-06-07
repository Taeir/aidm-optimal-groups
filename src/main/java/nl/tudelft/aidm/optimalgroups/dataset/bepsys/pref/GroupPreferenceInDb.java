package nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GroupPreferenceInDb implements GroupPreference
{
	private DataSource dataSource;
	private Integer userId;
	private CourseEdition courseEdition;

	private List<Agent> asList = null;
	private int[] preference = null;

	public GroupPreferenceInDb(DataSource dataSource, Integer userId, CourseEdition courseEdition)
	{
		this.dataSource = dataSource;
		this.userId = userId;
		this.courseEdition = courseEdition;
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

	@Override
	public List<Agent> asListOfAgents()
	{
		if (asList == null) {
			asList = Arrays.stream(asArray())
				.boxed()
				.map(friendAgentId -> courseEdition.allAgents().findByAgentId(friendAgentId).get())
				.collect(Collectors.toList());
		}

		return Collections.unmodifiableList(asList);
	}

	@Override
	public Integer count()
	{
		return asArray().length;
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
				.addParameter("courseEditionId", courseEdition.bepSysId());

			List<Integer> preferredStudents = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("student_id"));
			return preferredStudents;
		}
	}
}
