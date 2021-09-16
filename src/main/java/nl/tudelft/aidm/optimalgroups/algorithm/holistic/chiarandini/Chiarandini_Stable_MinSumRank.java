package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.StabilityConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.MinimizeSumOfRanks;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.UtilitarianWeightsObjective;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class Chiarandini_Stable_MinSumRank
{
	private final DatasetContext datasetContext;
	private final Pregrouping pregrouping;
	
	private UtilitarianWeightsObjective.WeightScheme weightScheme;
	
	public Chiarandini_Stable_MinSumRank(DatasetContext datasetContext, PregroupingType pregroupingType)
	{
		this.datasetContext = datasetContext;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
	}

	public AgentToProjectMatching doIt()
	{
		var objFn = new MinimizeSumOfRanks();
		var stability = new StabilityConstraint();
		
		return new ChiarandiniBaseModel(datasetContext, objFn, pregrouping.constraint(), stability).doIt();
	}
}
