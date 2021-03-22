package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives;

import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.UtilitarianWeightsObjective;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;

public record MinimizeSumOfExpRanks(SequentualDatasetContext datasetContext,
                                    AssignmentConstraints assignmentConstraints)
implements ObjectiveFunction
{
	@Override
	public void apply(GRBModel model)
	{
		var maxRank = datasetContext.worstRank().orElse(0);
		UtilitarianWeightsObjective.WeightScheme weightScheme = rank -> -1 * Math.pow(2, Math.max(0, maxRank - rank));
		
		var obj = new UtilitarianWeightsObjective(datasetContext, assignmentConstraints, weightScheme);
        obj.apply(model);
	}
	
	@Override
	public String name()
	{
		return "min sum exp-ranks";
	}
}
