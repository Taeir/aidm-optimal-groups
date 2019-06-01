package nl.tudelft.aidm.optimalgroups.model.entity;

import java.util.*;
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
	private Map<String, Agent> idToAgentsMap;

	private String courseEditionId;

	private Agents(List<Agent> agents)
	{
		this.agents = agents;

		idToAgentsMap = new HashMap<>(agents.size());
		for (Agent agent : agents)
		{
			idToAgentsMap.put(agent.name, agent);
		}
	}

	public int count()
	{
		return agents.size();
	}

	public Optional<Agent> findByAgentId(String name)
	{
		return Optional.ofNullable(idToAgentsMap.get(name));
	}

	public Collection<Agent> asCollection()
	{
		return agents;
	}

	public Agents with(Agents other)
	{
		ArrayList<Agent> copyAgents = new ArrayList<>(this.agents);
		copyAgents.addAll(other.agents);

		return new Agents(copyAgents);
	}

	// todo: move into Agent, nice but need reference to Agents so would require some refactoring

	/**
	 * Checks if the agent is included in preference lists of all agents <b>(that are also in this Agents collection)</b> that the agent has included in his own preference list
	 * TODO: introduce a class encompassing all the agents for a course edition for making this method safer to use
	 * @param agent
	 * @return
	 */
	public boolean hasEqualFriendLists(Agent agent)
	{
		Set<String> friends = new HashSet<String>();
		friends.add(agent.name); //Add agent himself to set

		for (int i : agent.groupPreference.asArray()) {
			friends.add(String.valueOf(i));
		}

		for (String friend : friends) {
			Set<String> friendsOfFriend = new HashSet<>();
			friendsOfFriend.add(friend); // Add friend himself to list

			for (int i : idToAgentsMap.get(friend).groupPreference.asArray()) {
				friendsOfFriend.add(String.valueOf(i));
			}

			if (friends.equals(friendsOfFriend) == false) {
				return false;
			}
		}

		return true;
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
				.addParameter("courseEditionId", courseEditionId);

			List<String> userIds = query.executeAndFetch((ResultSetHandler<String>) resultSet -> String.valueOf(resultSet.getInt("user_id")));
			List<Agent> agents = userIds.stream().map(id -> new Agent.fromDb(dataSource, id, String.valueOf(courseEditionId))).collect(Collectors.toList());

			return new Agents(agents);
		}
	}
}
