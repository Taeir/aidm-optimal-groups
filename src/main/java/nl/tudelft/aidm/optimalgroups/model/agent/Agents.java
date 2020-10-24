package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Collection class for Agent
 * TODO: Refactor into interface
 */
public class Agents
{
	public final DatasetContext datasetContext;

	// list for now
	private Set<Agent> agents;
	private Map<Integer, Agent> idToAgentsMap;


	public Agents(DatasetContext datasetContext, Collection<Agent> agents)
	{
		this.datasetContext = datasetContext;

		// Ensure agents are ordered in same way as given (yes, an Agent's interface and specific ordered impl would be nicer)
		this.agents = new LinkedHashSet<>(agents);

		// Better to change the ctor signature
		Assert.that(this.agents.size() == agents.size()).orThrowMessage("Agents contained duplicates");

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
		return Collections.unmodifiableCollection(agents);
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

	public Agents with(Agent other)
	{
		return this.with(Agents.from(other));
	}

	public Agents with(Collection<Agent> other)
	{
		return this.with(Agents.from(other));
	}

	public Agents with(Agents other)
	{
		Assert.that(datasetContext.equals(other.datasetContext)).orThrowMessage("Cannot combine Agents: datasetcontext mismatch");

		ArrayList<Agent> copyAgents = new ArrayList<>(this.agents);
		copyAgents.addAll(other.agents);

		return new Agents(datasetContext, copyAgents);
	}

	public Agents without(Collection<Agent> other)
	{
		return this.without(Agents.from(other));
	}

	public Agents without(Agents other)
	{
		if (other.count() == 0) return this;

		Assert.that(datasetContext.equals(other.datasetContext)).orThrowMessage("Cannot remove Agents: datasetcontext mismatch");

		ArrayList<Agent> copyAgents = new ArrayList<>(this.agents);
		copyAgents.removeAll(other.agents);

		return new Agents(datasetContext, copyAgents);
	}

	public void forEach(Consumer<Agent> fn)
	{
		agents.forEach(fn);
	}

	// todo: move into Agent, nice but need reference to Agents so would require some refactoring

	/**
	 * Checks if the agent is included in preference lists of all agents <b>(that are also in this Agents collection)</b> that the agent has included in his own preference list
	 * <br /> TODO: Move into peer preferences class
	 * @param agent
	 * @return
	 */
	public boolean hasEqualFriendLists(Agent agent)
	{
		if (agent.groupPreference.count() == 0) {
			// No preference, therefore also no clique
			return false;
		}

		// Function that maps the Agent's "group preferences" to a Set of agents (including himself)
		// intuitively, this is the agent's proposal for a (partial) group formation.
		Function<Agent, Set<Agent>> agentPreferencesToProposedGroup = (Agent x) -> {
			var groupProposal = new HashSet<Agent>();
			//Add agent himself to set to make comparing preferences easy
			groupProposal.add(x);
			groupProposal.addAll(x.groupPreference.asListOfAgents());

			return groupProposal;
		};

		// The proposal of the given agent
		var proposedGroupOfAgent = agentPreferencesToProposedGroup.apply(agent);

		// If all the agents that are in the proposal of 'agent' have _exactly_ the
		// same proposals, then
		var agentProposalIsCompletelyMutual = agent.groupPreference.asListOfAgents().stream()
			.map(agentPreferencesToProposedGroup)
			.allMatch(proposedGroupOfAgent::equals);

		return agentProposalIsCompletelyMutual;
	}

	public static Agents from(Agent... agents)
	{
		return Agents.from(List.of(agents));
	}

	public static Agents from(Collection<Agent> agents)
	{
		var datasetContext = agents.stream().map(agent -> agent.context)
			.findAny().orElseGet(() -> {
				System.out.print("Warning: creating Agents from empty collection, datasetContext is set to null!\n");
				return null;
			});

		return new Agents(datasetContext, agents);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Agents)) return false;
		Agents other = (Agents) o;
		return datasetContext.equals(other.datasetContext) &&
			agents.equals(other.agents);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(agents, datasetContext);
	}
}
