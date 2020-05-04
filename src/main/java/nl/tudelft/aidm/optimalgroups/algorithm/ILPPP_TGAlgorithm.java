package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp.ILPPPDeterminedMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;

public class ILPPP_TGAlgorithm implements TopicGroupAlgorithm
{
	@Override
	public String name()
	{
		return "ILPPP";
	}

	@Override
	public GroupProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
	{
		return new ILPPPDeterminedMatching(datasetContext);
	}

	@Override
	public String toString()
	{
		return name();
	}
}
