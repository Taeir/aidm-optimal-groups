package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.project.da.SPDAMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class DeferredAccpetanceSPAlgorithm implements StudentProjectAlgorithm
{
	@Override
	public String name()
	{
		return "Capacitated DA";
	}

	@Override
	public AgentToProjectMatching determineMatching(DatasetContext datasetContext)
	{
		return new SPDAMatching(datasetContext);
	}
}
