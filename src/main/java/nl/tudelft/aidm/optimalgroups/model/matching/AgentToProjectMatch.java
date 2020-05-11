package nl.tudelft.aidm.optimalgroups.model.matching;

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

	public Agent agent()
	{
		return from();
	}

	public Project project()
	{
		return to();
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

	public static AgentToProjectMatch from(Match<Agent, Project> match)
	{
		if (match instanceof AgentToProjectMatch) {
			return (AgentToProjectMatch) match;
		}

		return new AgentToProjectMatch(match.from(), match.to());
	}
}
