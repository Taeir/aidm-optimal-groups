package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMaxFlow;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;

public class BepSysTGAlgorithm implements TopicGroupAlgorithm
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

	/*
	private static GroupProjectMatching<Group.FormedGroup> assignGroupsToProjects(DatasetContext datasetContext, GroupFormingAlgorithm formedGroups)
	{
		if (projectAssignmentAlgorithm.equals("RSD")) {
			return new RandomizedSerialDictatorship(formedGroups.asFormedGroups(), datasetContext.allProjects());
		} else {
			return new GroupProjectMaxFlow(formedGroups.asFormedGroups(), datasetContext.allProjects());
		}
	}

	private static GroupFormingAlgorithm formGroups(DatasetContext courseEdition)
	{
		if (groupMatchingAlgorithm.equals("CombinedPreferencesGreedy")) {
			return new CombinedPreferencesGreedy(courseEdition);
		}
		else if (groupMatchingAlgorithm.equals("BEPSysFixed")) {
			return new BepSysImprovedGroups(courseEdition.allAgents(), courseEdition.groupSizeConstraint(), true);
		}
		else {
			return new BepSysImprovedGroups(courseEdition.allAgents(), courseEdition.groupSizeConstraint(), false);
		}
	}

	* */
}
