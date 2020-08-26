package nl.tudelft.aidm.optimalgroups.model.dataset;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.lang.exception.ImplementMe;

public class ManualDatasetContext implements DatasetContext
{
	private final String name;
	private final Projects projects;
	private final Agents agents;
	private final GroupSizeConstraint groupSizeConstraint;

	public ManualDatasetContext(String name, Projects projects, Agents agents, GroupSizeConstraint groupSizeConstraint)
	{
		this.name = name;
		this.projects = projects;
		this.agents = agents;
		this.groupSizeConstraint = groupSizeConstraint;
	}

	@Override
	public String identifier()
	{
		return name;
	}

	@Override
	public Projects allProjects()
	{
		return projects;
	}

	@Override
	public Agents allAgents()
	{
		return agents;
	}

	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		return groupSizeConstraint;
	}
}
