package nl.tudelft.aidm.optimalgroups.model.agent;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;

/**
 * Collection class for Agent
 */
public class Agents
{
	// list for now
	private List<Agent> agents;
	private Map<Integer, Agent> idToAgentsMap;

	private String courseEditionId;

	public Agents(List<Agent> agents)
	{
		this.agents = agents;

		idToAgentsMap = new HashMap<>(agents.size());
		for (Agent agent : agents)
		{
			idToAgentsMap.put(agent.id, agent);
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

	public void useCombinedPreferences() {
		for (Agent a : this.asCollection()) {
			a.replaceProjectPreferenceWithCombined(this);
		}
	}

	public void useDatabasePreferences() {
		for (Agent a : this.asCollection()) {
			a.useDatabaseProjectPreferences();
		}
	}

	public Agents with(Agents other)
	{
		ArrayList<Agent> copyAgents = new ArrayList<>(this.agents);
		copyAgents.addAll(other.agents);

		return new Agents(copyAgents);
	}

	public void forEach(Consumer<Agent> fn)
	{
		agents.forEach(fn);
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
		var friends = new HashSet<Integer>();
		friends.add(agent.id); //Add agent himself to set

		for (int i : agent.groupPreference.asArray()) {
			friends.add(i);
		}

		// If friends only contain himself, prevent forming a clique
		if (friends.size() == 1) {
			return false;
		}

		for (var friend : friends) {
			if (idToAgentsMap.containsKey(friend) == false) {
				// friend is not part of this 'Agents' set therefore the lists are not equal
				return false;
			}

			var friendsOfFriend = new HashSet<Integer>();
			friendsOfFriend.add(friend); // Add friend himself to list


			for (int i : idToAgentsMap.get(friend).groupPreference.asArray()) {
				friendsOfFriend.add(i);
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
	public static Agents fromBepSysDb(DataSource dataSource, int courseEditionId)
	{
		var sql2o = new Sql2o(dataSource);
		try (var connection = sql2o.open()) {
			var query = connection.createQuery("SELECT distinct cp.user_id as user_id\n" +
				"FROM course_participations cp \n" +
				"WHERE cp.course_edition_id = :courseEditionId")
				.addParameter("courseEditionId", courseEditionId);

			List<Integer> userIds = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("user_id"));
			List<Agent> agents = userIds.stream().map(id ->
					Agent.AgentInBepSysSchemaDb.from(dataSource, id, String.valueOf(courseEditionId)))
					.collect(Collectors.toList());

			return new Agents(agents);
		}
	}
}
