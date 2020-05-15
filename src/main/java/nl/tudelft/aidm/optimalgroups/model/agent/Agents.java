package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.function.Consumer;

/**
 * Collection class for Agent
 */
public class Agents
{
	public final DatasetContext datsetContext;

	// list for now
	private List<Agent> agents;
	private Map<Integer, Agent> idToAgentsMap;


	public Agents(DatasetContext datsetContext, List<Agent> agents)
	{
		this.datsetContext = datsetContext;
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

	public Optional<Agent> findByAgentId(Integer agentId)
	{
		return Optional.ofNullable(idToAgentsMap.get(agentId));
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
		Assert.that(datsetContext.equals(other.datsetContext)).orThrowMessage("Cannot combine Agents: datasetcontext mismatch");

		ArrayList<Agent> copyAgents = new ArrayList<>(this.agents);
		copyAgents.addAll(other.agents);

		return new Agents(datsetContext, copyAgents);
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
		return Agents.from(List.of(agents));
	}

	public static Agents from(List<Agent> agents)
	{
		var datasetContext = agents.get(0).context;
		return new Agents(datasetContext, agents);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Agents)) return false;
		Agents other = (Agents) o;
		return datsetContext.equals(other.datsetContext) &&
			agents.equals(other.agents);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(agents, datsetContext);
	}
}
