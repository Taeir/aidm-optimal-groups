package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives;

import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.UtilitarianWeightsObjective;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;

public record MinimizeSumOfRanks(SequentualDatasetContext sequentualDatasetContext,
                                 AssignmentConstraints assignmentConstraints)
implements ObjectiveFunction
{
	@Override
	public void apply(GRBModel model)
	{
		var obj = new UtilitarianWeightsObjective(sequentualDatasetContext, assignmentConstraints, rank -> rank);
		obj.apply(model);
	}
	
	@Override
	public String name()
	{
		return "min sum of ranks";
	}
}
