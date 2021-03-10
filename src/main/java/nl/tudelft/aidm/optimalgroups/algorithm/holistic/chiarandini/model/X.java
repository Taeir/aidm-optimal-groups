package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import gurobi.GRB;
import gurobi.GRBModel;
import gurobi.GRBVar;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public record X(Agent student, Project project, Project.ProjectSlot slot, String name, GRBVar asVar)
{
	public static X createInModel(Agent student, Project project, Project.ProjectSlot slot, GRBModel model)
	{
		var name = String.format("x[%s, %s]", student, slot);
		var grbVar = GurobiHelperFns.makeBinaryVariable(model, name);

		return new X(student, project, slot, name, grbVar);
	}

	public double getValueOrThrow()
	{
		return Try.getting(() -> this.asVar().get(GRB.DoubleAttr.X))
			.or(Rethrow.asRuntime());
	}

	@Override
	public String toString()
	{
		return name;
	}
}
