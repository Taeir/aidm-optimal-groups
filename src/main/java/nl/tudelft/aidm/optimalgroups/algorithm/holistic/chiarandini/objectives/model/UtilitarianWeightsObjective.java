package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public record UtilitarianWeightsObjective(AssignmentConstraints assignmentVars, WeightScheme weightScheme)
{
	public interface WeightScheme
	{
		double weight(int rank);
	}
	
	public void apply(GRBModel model)
	{
		Try.doing(
			() -> applyDirty(model)
		).or(Rethrow.asRuntime());
	}
	
	private void applyDirty(GRBModel model) throws GRBException
	{
		var datasetContext = assignmentVars.datasetContext;
		var objFnExpr = new GRBLinExpr();

		datasetContext.allAgents().forEach(agent -> {
			agent.projectPreference().forEach((project, rank) -> {
				project.slots().forEach(slot -> {

					// Agent is not indiff and finds project acceptable
					if (!rank.isCompletelyIndifferent() && !rank.unacceptable()) {
						var x = assignmentVars.xVars.of(agent, slot).orElseThrow();

						// calc weight from rank
						var weight = weightScheme.weight(rank.asInt());
						objFnExpr.addTerm(weight, x.asVar());
					}
					
				});

				return true; // continue iter
			});
		});

		model.setObjective(objFnExpr, GRB.MINIMIZE);
	}
}
