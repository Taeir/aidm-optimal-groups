package nl.tudelft.aidm.optimalgroups.model.entity;

import java.util.Collection;
import java.util.List;

/**
 * Collection class for Agent
 */
public class Agents
{
	// list for now
	private List<Agent> agents;

	private Agents(List<Agent> agents)
	{
		this.agents = agents;
	}

	public Collection<Agent> asCollection()
	{
		return agents;
	}

	public static Agents from(Agent... agents)
	{
		return new Agents(List.of(agents));
	}
}
