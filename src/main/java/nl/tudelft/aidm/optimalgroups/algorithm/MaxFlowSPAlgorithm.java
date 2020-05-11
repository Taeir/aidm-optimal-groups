package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMaxFlowMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class MaxFlowSPAlgorithm implements StudentProjectAlgorithm
{
	@Override
	public String name()
	{
		return "Maxflow";
	}

	@Override
	public AgentToProjectMatching determineMatching(DatasetContext datasetContext)
	{
		return new StudentProjectMaxFlowMatching(datasetContext);
	}
}
