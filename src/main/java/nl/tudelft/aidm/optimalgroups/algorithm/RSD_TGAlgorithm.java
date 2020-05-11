package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.RandomizedSerialDictatorship;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;

public class RSD_TGAlgorithm implements TopicGroupAlgorithm
{
	@Override
	public String name()
	{
		return "BepSys groups into Randomised Serial Dictatorship (IA with Random lottery)";
	}

	@Override
	public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
	{
		var formedGroups = new BepSysImprovedGroups(datasetContext.allAgents(), datasetContext.groupSizeConstraint(), true).asFormedGroups();
		var matching = new RandomizedSerialDictatorship(datasetContext, formedGroups, datasetContext.allProjects());

		return matching;
	}

	@Override
	public String toString()
	{
		return name();
	}
}
