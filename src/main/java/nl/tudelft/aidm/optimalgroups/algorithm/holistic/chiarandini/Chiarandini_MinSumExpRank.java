package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.MinimizeSumOfExpRanks;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.UtilitarianWeightsObjective;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

public class Chiarandini_MinSumExpRank
{
	private final DatasetContext datasetContext;
	private final Pregrouping pregrouping;
	
	private UtilitarianWeightsObjective.WeightScheme weightScheme;

	public Chiarandini_MinSumExpRank(DatasetContext datasetContext, PregroupingType pregroupingType)
	{
		this.datasetContext = datasetContext;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
	}

	public GroupToProjectMatching<Group.FormedGroup> doIt()
	{
		var objFn = new MinimizeSumOfExpRanks();
		
		return new ChiarandiniBaseModel(datasetContext, objFn, pregrouping.constraint()).doIt();
	}
}
