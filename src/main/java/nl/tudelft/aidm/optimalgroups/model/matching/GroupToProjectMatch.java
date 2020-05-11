package nl.tudelft.aidm.optimalgroups.model.matching;

import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class GroupToProjectMatch<G extends Group> implements Match<G, Project>
{
	private final G group;
	private final Project project;

	public GroupToProjectMatch(G group, Project project)
	{
		this.group = group;
		this.project = project;
	}

	public Group group()
	{
		return group;
	}

	public Project project()
	{
		return project;
	}

	@Override
	public G from()
	{
		return group;
	}

	@Override
	public Project to()
	{
		return project;
	}
}
