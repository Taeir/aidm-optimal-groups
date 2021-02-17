package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;

public class UtilitarianWeightsObjective
{
	public static void createInModel(GRBModel model, SequentualDatasetContext datasetContext,
	                                 AssignmentConstraint assignmentVars, WeightScheme weightScheme)
		throws GRBException
	{
		var objFnExpr = new GRBLinExpr();

		datasetContext.allAgents().forEach(agent -> {
			agent.projectPreference().forEach((project, rank) -> {
				project.slots().forEach(slot -> {

					// Agent is not indiff and finds project acceptable
					if (!rank.isCompletelyIndifferent() && !rank.unacceptable()) {
						var x = assignmentVars.x(agent, slot).get();

						// calc weight from rank
						var weight = weightScheme.weight(rank.asInt());
						objFnExpr.addTerm(weight, x.asVar());
					}

				});

			});
		});

		model.setObjective(objFnExpr, GRB.MINIMIZE);
	}

	interface WeightScheme
	{
		public double weight(int rank);
	}
}
