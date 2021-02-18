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
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public class Chiarandini_MinimaxDistribOWA
{
	private final DatasetContext datasetContext;
	private final DistributiveWeightsObjective.WeightScheme weightScheme;

	public Chiarandini_MinimaxDistribOWA(DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
		this.weightScheme = new DistribOWAWeightScheme(datasetContext);
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

		AssignmentConstraint assignmentConstraint = AssignmentConstraint.createInModel(model, seqDatasetContext);
		DistributiveWeightsObjective.createInModel(model, seqDatasetContext, assignmentConstraint, weightScheme);

		model.optimize();

		// extract x's and map to matching
		var matching = new AgentToProjectMatching(assignmentConstraint, model, seqDatasetContext);

		env.dispose();

		return matching;
	}

	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);

		var owaMinimaxChiarandini = new Chiarandini_MinimaxDistribOWA(ce);
		var resultOwa = owaMinimaxChiarandini.doIt();

		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankDistributionInMatching(resultOwa).displayChart("Chiarandini minimax-owa");

		return;
	}


	public static class DistribOWAWeightScheme implements DistributiveWeightsObjective.WeightScheme// withMinimaxOWAScheme(DatasetContext datasetContext)
	{
		private final int delta;
		private final double Beta;

		public DistribOWAWeightScheme(DatasetContext datasetContext)
		{
			// delta symbol = max pref length in paper
			// TODO: Prefs max rank method
			delta = 8; //datasetContext.allProjects().count();
			Beta = 1d/delta - 0.001d;
		}

		private double f_(int rank)
		{
			return rank * 1d / delta;
		}

		@Override
		public double weight(int rank)
		{
			final double rescale = 1;
			if (rank > delta) return rank * 10000;

			Assert.that(rank >= 1 && rank <= delta).orThrowMessage("Rank must be 1 <= rank <= delta, cannot calc weight. Bug?");

			if (rank == 1)
				return rescale * f_(rank) * Math.pow(Beta, delta - 1
					/ Math.pow(1 + Beta, delta - 1));

			else
				return rescale * f_(rank) * Math.pow(Beta, delta - rank)
					/ Math.pow(1 + Beta, delta + 1 - rank);
		}
	}

}
