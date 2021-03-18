package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.StabilityConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ChiarandiniAgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.MinimizeSumOfExpRanks;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.UtilitarianWeightsObjective;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public class Chiarandini_MinSumExpRank
{
	private final DatasetContext datasetContext;
	private UtilitarianWeightsObjective.WeightScheme weightScheme;

	public Chiarandini_MinSumExpRank(DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
	}

	public nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching doIt()
	{
		return Try.getting(this::doItDirty)
			.or(Rethrow.asRuntime());
	}

	public nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching doItDirty() throws GRBException
	{
		var seqDatasetContext = SequentualDatasetContext.from(datasetContext);

		var env = new GRBEnv();
		env.start();

		var model = new GRBModel(env);

		AssignmentConstraints assignmentConstraints = AssignmentConstraints.createInModel(model, seqDatasetContext);
		
		var objFn = new MinimizeSumOfExpRanks(seqDatasetContext, assignmentConstraints);
		objFn.apply(model);
		
		model.optimize();

		// extract x's and map to matching

		var matching = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDatasetContext);

		env.dispose();

		return matching.original();
	}
}
