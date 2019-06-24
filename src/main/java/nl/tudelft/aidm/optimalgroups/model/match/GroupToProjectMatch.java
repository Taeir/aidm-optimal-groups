package nl.tudelft.aidm.optimalgroups.model.match;

import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.Project;

public class GroupToProjectMatch<G extends Group> implements Match<G, Project>
{
	private final G group;
	private final Project project;

	public GroupToProjectMatch(G group, Project project)
	{
		this.group = group;
		this.project = project;
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
