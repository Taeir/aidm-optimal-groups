package nl.tudelft.aidm.optimalgroups.model.match;

import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;

public class AgentToProjectMatch implements Match<Agent, Project>
{
	private final Agent agent;
	private final Project project;

	public AgentToProjectMatch(Agent agent, Project project)
	{
		this.agent = agent;
		this.project = project;
	}

	@Override
	public Agent from()
	{
		return agent;
	}

	@Override
	public Project to()
	{
		return project;
	}
}
