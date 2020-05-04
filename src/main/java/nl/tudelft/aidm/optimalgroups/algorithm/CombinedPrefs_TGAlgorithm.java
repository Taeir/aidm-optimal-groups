package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.group.CombinedPreferencesGreedy;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMaxFlow;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;

public class CombinedPrefs_TGAlgorithm implements TopicGroupAlgorithm
{
	@Override
	public String name()
	{
		return "Peer and Topic preferences merging";
	}

	@Override
	public GroupProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
	{
		var formedGroups = new CombinedPreferencesGreedy(datasetContext).asFormedGroups();
		var matching = new GroupProjectMaxFlow(datasetContext, formedGroups, datasetContext.allProjects());

		return matching;
	}

	@Override
	public String toString()
	{
		return name();
	}
}
