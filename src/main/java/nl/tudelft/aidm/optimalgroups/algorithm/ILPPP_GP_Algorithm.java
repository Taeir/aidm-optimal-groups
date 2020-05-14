package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp.ILPPPDeterminedMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;

public class ILPPP_GP_Algorithm implements GroupProjectAlgorithm
{
	@Override
	public String name()
	{
		return "ILPPP";
	}

	@Override
	public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
	{
		return new ILPPPDeterminedMatching(datasetContext);
	}

	@Override
	public String toString()
	{
		return name();
	}
}
