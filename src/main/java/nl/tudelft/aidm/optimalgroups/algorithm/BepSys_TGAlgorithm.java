package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMaxFlow;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;

public class BepSys_TGAlgorithm implements TopicGroupAlgorithm
{
	@Override
	public String name()
	{
		// TODO include Pref agg method
		return "BepSys";
	}

	@Override
	public GroupProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
	{
		var groups = new BepSysImprovedGroups(datasetContext.allAgents(), datasetContext.groupSizeConstraint(), true);
		var groupsToProjects = new GroupProjectMaxFlow(datasetContext, groups.asFormedGroups(), datasetContext.allProjects());

		return groupsToProjects;
	}

	@Override
	public String toString()
	{
		return name();
	}
}
