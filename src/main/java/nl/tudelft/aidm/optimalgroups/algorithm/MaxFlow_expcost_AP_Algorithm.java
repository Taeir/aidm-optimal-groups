package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.project.AgentProjectMaxFlowMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class MaxFlow_expcost_AP_Algorithm implements AgentProjectAlgorithm
{
	@Override
	public String name()
	{
		return "Maxflow (exp cost)";
	}

	@Override
	public AgentToProjectMatching determineMatching(DatasetContext datasetContext)
	{
		PreferencesToCostFn preferencesToCostFn = (projectPreference, theProject) -> (int) Math.pow(projectPreference.rankOf(theProject), 2);
		return new AgentProjectMaxFlowMatching(datasetContext, preferencesToCostFn);
	}
}
