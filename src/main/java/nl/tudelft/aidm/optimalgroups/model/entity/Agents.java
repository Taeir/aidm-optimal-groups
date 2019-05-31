package nl.tudelft.aidm.optimalgroups.model.entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;

import javax.sql.DataSource;

/**
 * Collection class for Agent
 */
public class Agents
{
	// list for now
	private List<Agent> agents;
	private String courseEditionId;

	private Agents(List<Agent> agents)
	{
		this.agents = agents;
	}

	public int count()
	{
		return agents.size();
	}

	public Collection<Agent> asCollection()
	{
		return agents;
	}

	public static Agents from(Agent... agents)
	{
		return new Agents(List.of(agents));
	}

	public static Agents from(List<Agent> agents)
	{
		return new Agents(agents);
	}

	/**
	 * Fetches Agents and their preferences from the given DataSource
	 * <br />
	 * Assumes the datasource is the bepsys database dump. Refactor when more datasource types are present</p>
	 * @param dataSource
	 * @return
	 */
	public static Agents from(DataSource dataSource, int courseEditionId)
	{
		var sql2o = new Sql2o(dataSource);
		try (var connection = sql2o.open()) {
			var query = connection.createQuery("SELECT distinct users.id as user_id\n" +
				"FROM users\n" +
				"JOIN project_preferences ON users.id = project_preferences.user_id\n" +
				"where course_edition_id = :courseEditionId")
				.addParameter(":courseEditionId", courseEditionId);

			List<String> userIds = query.executeAndFetch((ResultSetHandler<String>) resultSet -> String.valueOf(resultSet.getInt("user_id")));
			List<Agent> agents = userIds.stream().map(id -> new Agent.fromDb(dataSource, id, String.valueOf(courseEditionId))).collect(Collectors.toList());

			return new Agents(agents);
		}
	}
}
