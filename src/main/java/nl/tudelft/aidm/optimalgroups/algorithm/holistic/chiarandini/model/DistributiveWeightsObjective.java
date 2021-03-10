package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;

/**
 * Weight is applied over the bucket per rank, "the distributive approach"
 * Used for example in OWA-Distrib as the individual approach may run into numerical issues
 */
public class DistributiveWeightsObjective
{
	public static void createInModel(GRBModel model, SequentualDatasetContext datasetContext,
	                                 AssignmentConstraints assignmentVars, WeightScheme weightScheme)
		throws GRBException
	{
		var objFnExpr = new GRBLinExpr();

		for (int i = 1; i <= datasetContext.allProjects().count(); i++)
		{
			final var h = i;
			var numStudentsWithRankH = new GRBLinExpr();

			datasetContext.allAgents().forEach(agent -> {
				agent.projectPreference().forEach((project, rank) -> {
					project.slots().forEach(slot -> {

						// Agent is not indiff and finds project acceptable
						if (!rank.isCompletelyIndifferent() && !rank.unacceptable() && rank.asInt() == h) {
							var x = assignmentVars.xVars.of(agent, slot).orElseThrow();
							numStudentsWithRankH.addTerm(1d, x.asVar());
						}
					});

				});
			});

			// calc weight from rank
			var weight = weightScheme.weight(h);
			objFnExpr.multAdd(weight, numStudentsWithRankH);
		}

		model.setObjective(objFnExpr, GRB.MINIMIZE);
	}

	public interface WeightScheme
	{
		double weight(int rank);
	}
}
