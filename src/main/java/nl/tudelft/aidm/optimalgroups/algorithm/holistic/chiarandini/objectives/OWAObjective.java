package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives;

import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.DistributiveWeightsObjective;
import plouchtch.assertion.Assert;

public class OWAObjective implements ObjectiveFunction
{
	@Override
	public void apply(GRBModel model, AssignmentConstraints assignmentConstraints)
	{
		var owaWeightingScheme = new OWA_distributiveWeightScheme();
		var obj = new DistributiveWeightsObjective(assignmentConstraints, owaWeightingScheme);

		obj.apply(model);
	}
	
	@Override
	public String name()
	{
		return "owa";
	}
	
	private static class OWA_distributiveWeightScheme implements DistributiveWeightsObjective.WeightScheme// withMinimaxOWAScheme(DatasetContext datasetContext)
	{
		private final int delta;
		private final double Beta;
		
		public OWA_distributiveWeightScheme()
		{
			// delta symbol = max pref length in paper
			// TODO: Prefs max rank method
			delta = 8; //sequentualDatasetContext.allProjects().count();
			Beta = 1d / delta - 0.001d;
		}
		
		private double f_(int rank)
		{
			return rank * 1d / delta;
		}
		
		@Override
		public double weight(int rank)
		{
			final double rescale = 1;
			if (rank > delta)
				return rank * 10000;
			
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
