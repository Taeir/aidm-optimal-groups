package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualAgents;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualProjects;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

/**
 * Weight is applied over the bucket per rank, "the distributive approach"
 * Used for example in OWA-Distrib as the individual approach may run into numerical issues
 */
public record DistributiveWeightsObjective(AssignmentConstraints assignmentVars, WeightScheme weightScheme)
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
		var seqDatasetContext = assignmentVars.datasetContext;
		var allProjects = seqDatasetContext.allProjects();
		var allStudents = seqDatasetContext.allAgents();
		
		var objFnExpr = new GRBLinExpr();
		
		for (int i = 1; i <= allProjects.count(); i++)
		{
			final var h = i;
			var numStudentsWithRankH = new GRBLinExpr();
			
			allStudents.forEach(agent -> {
				agent.projectPreference().forEach((project, rank) -> {
					project.slots().forEach(slot -> {
						
						// Agent is not indiff and finds project acceptable
						if (rank.isPresent() && rank.asInt() == h) {
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
}
