package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.MinimizeSumOfRanks;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.UtilitarianWeightsObjective;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

public class Chiarandini_MinSumRank
{
	private final DatasetContext datasetContext;
	private final Pregrouping pregrouping;
	
	private UtilitarianWeightsObjective.WeightScheme weightScheme;

	public Chiarandini_MinSumRank(DatasetContext datasetContext, PregroupingType pregroupingType)
	{
		this.datasetContext = datasetContext;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
	}

	public GroupToProjectMatching<Group.FormedGroup> doIt()
	{
		var objFn = new MinimizeSumOfRanks();
		
		return new ChiarandiniBaseModel(datasetContext, objFn, pregrouping.constraint()).doIt();
	}
}
