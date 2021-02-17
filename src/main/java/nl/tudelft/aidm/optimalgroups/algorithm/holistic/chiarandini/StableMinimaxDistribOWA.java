package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.rank.distribution.StudentRankDistributionInMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import plouchtch.assertion.Assert;

public class StableMinimaxDistribOWA
{
	private final DatasetContext datasetContext;
	private final DistributiveWeightsObjective.WeightScheme weightScheme;

	public StableMinimaxDistribOWA(DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
		this.weightScheme = new ChiarandiniMinimaxDistribOWA.DistribOWAWeightScheme(datasetContext);
	}

	public nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching doIt() throws GRBException
	{
		var seqDatasetContext = SequentualDatasetContext.from(datasetContext);

		var env = new GRBEnv();
		env.start();

		var model = new GRBModel(env);

		AssignmentConstraint assignmentConstraint = AssignmentConstraint.createInModel(model, seqDatasetContext);
		DistributiveWeightsObjective.createInModel(model, seqDatasetContext, assignmentConstraint, weightScheme);

		var stability = StabilityConstraints.createInModel(model, assignmentConstraint, seqDatasetContext);
		stability.createStabilityFeasibilityConstraint(model);

		model.optimize();

		// extract x's and map to matching

		var matching = new AgentToProjectMatching(assignmentConstraint, model, seqDatasetContext);

		env.dispose();

		return matching;
	}

	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);


		var owaMinimaxChiarandini = new ChiarandiniMinimaxDistribOWA(ce);
		var resultOwa = owaMinimaxChiarandini.doIt();

		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankDistributionInMatching(resultOwa).displayChart("Chiarandini minimax-owa");


		var stableOwaMinimaxChiarandini = new StableMinimaxDistribOWA(ce);
		var resultStableOwa = stableOwaMinimaxChiarandini.doIt();

		var metricsStableOwa = new MatchingMetrics.StudentProject(resultStableOwa);
		new StudentRankDistributionInMatching(resultStableOwa).displayChart("Chiarandini stable_minimax-owa");

		return;
	}

}
