package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.Constraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ChiarandiniAgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import plouchtch.assertion.Assert;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public record ChiarandiniBaseModel(DatasetContext datasetContext, ObjectiveFunction objectiveFunction, Constraint... constraints)
{
	public AgentToProjectMatching doIt()
	{
		return Try.getting(this::doItDirty)
			.or(Rethrow.asRuntime());
	}

	private AgentToProjectMatching doItDirty() throws GRBException
	{
		var env = new GRBEnv();
		env.start();

		var model = new GRBModel(env);

		AssignmentConstraints assignmentConstraints = AssignmentConstraints.createInModel(model, datasetContext);
		objectiveFunction.apply(model, assignmentConstraints);

		// Apply all constraints
		for (Constraint constraint : constraints)
		{
			model.update();
			constraint.apply(model, assignmentConstraints);
		}

		model.optimize();
		
		var status = model.get(GRB.IntAttr.Status);
		
		Assert.that(status == GRB.OPTIMAL).orThrowMessage("Model not solved");

		// extract x's and map to matching
		var matching = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, datasetContext);

		env.dispose();

		return matching;
	}
}
