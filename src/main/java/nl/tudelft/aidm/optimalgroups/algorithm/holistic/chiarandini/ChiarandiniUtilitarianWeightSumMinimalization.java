package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.rank.distribution.StudentRankDistributionInMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;

public class ChiarandiniUtilitarianWeightSumMinimalization
{
	private final DatasetContext datasetContext;
	private UtilitarianWeightsObjective.WeightScheme weightScheme;

	public static ChiarandiniUtilitarianWeightSumMinimalization withIdentityWeightScheme(DatasetContext datasetContext)
	{
		return new ChiarandiniUtilitarianWeightSumMinimalization(datasetContext, rank -> rank);
	}

	public static ChiarandiniUtilitarianWeightSumMinimalization withExpWeightScheme(DatasetContext datasetContext)
	{
		var maxRank = datasetContext.allProjects().count();
		UtilitarianWeightsObjective.WeightScheme weightScheme = rank -> -1 * Math.pow(2, Math.max(0, maxRank - rank));

		return new ChiarandiniUtilitarianWeightSumMinimalization(datasetContext, weightScheme);
	}

	public ChiarandiniUtilitarianWeightSumMinimalization(DatasetContext datasetContext, UtilitarianWeightsObjective.WeightScheme weightScheme)
	{
		this.datasetContext = datasetContext;
		this.weightScheme = weightScheme;
	}

	public nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching doIt() throws GRBException
	{
		var seqDatasetContext = SequentualDatasetContext.from(datasetContext);

		var env = new GRBEnv();
		env.start();

		var model = new GRBModel(env);

		AssignmentConstraint assignmentConstraint = AssignmentConstraint.createInModel(model, seqDatasetContext);
		UtilitarianWeightsObjective.createInModel(model, seqDatasetContext, assignmentConstraint, weightScheme);

		model.optimize();

		// extract x's and map to matching

		var matching = new AgentToProjectMatching(assignmentConstraint, model, seqDatasetContext);

		env.dispose();

		return matching;
	}

	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		var utilitarianChiarandini = ChiarandiniUtilitarianWeightSumMinimalization.withIdentityWeightScheme(ce);
		var result = utilitarianChiarandini.doIt();

		var metrics = new MatchingMetrics.StudentProject(result);
		new StudentRankDistributionInMatching(result).displayChart("Chiarandini util identity weight scheme");


		var expUtilitarianChiarandini = ChiarandiniUtilitarianWeightSumMinimalization.withExpWeightScheme(ce);
		var resultExp = expUtilitarianChiarandini.doIt();

		var metricsExp = new MatchingMetrics.StudentProject(resultExp);
//		new StudentRankDistributionInMatching(resultExp).displayChart("Chiarandini util exp weight scheme");


		var owaMinimaxChiarandini = new ChiarandiniMinimaxDistribOWA(ce);
		var resultOwa = owaMinimaxChiarandini.doIt();

		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankDistributionInMatching(resultOwa).displayChart("Chiarandini minimax-owa");

		return;
	}

}
