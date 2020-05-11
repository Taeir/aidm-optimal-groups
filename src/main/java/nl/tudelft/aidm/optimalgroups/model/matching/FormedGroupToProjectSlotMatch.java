package nl.tudelft.aidm.optimalgroups.model.matching;

import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

// The match tuple
public class FormedGroupToProjectSlotMatch implements Match<Group.FormedGroup, Project.ProjectSlot>
{
	Group.FormedGroup group;
	Project.ProjectSlot project;

	public FormedGroupToProjectSlotMatch(Group.FormedGroup group, Project.ProjectSlot slot)
	{
		this.group = group;
		this.project = slot;
	}

	@Override
	public Group.FormedGroup from()
	{
		return group;
	}

	@Override
	public Project.ProjectSlot to()
	{
		return project;
	}

	public Group group()
	{
		return group;
	}

	public Project.ProjectSlot projectSlot()
	{
		return project;
	}
}
