package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.MinimizeSumOfRanks;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.UtilitarianWeightsObjective;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class Chiarandini_MinSumRank
{
	private final DatasetContext datasetContext;
	private UtilitarianWeightsObjective.WeightScheme weightScheme;

	public Chiarandini_MinSumRank(DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
	}

	public AgentToProjectMatching doIt()
	{
		var objFn = new MinimizeSumOfRanks();
		
		return new ChiarandiniBaseModel(datasetContext, objFn).doIt();
	}
}
