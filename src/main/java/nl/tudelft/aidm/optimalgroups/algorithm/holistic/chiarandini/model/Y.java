package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import gurobi.GRBModel;
import gurobi.GRBVar;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public record Y(Project.ProjectSlot project, String name, GRBVar asVar)
{
	public static Y createInModel(Project.ProjectSlot slot, GRBModel model)
	{
		var name = String.format("y[%s]", slot.toString());
		var grbVar = GurobiHelperFns.makeBinaryVariable(model, name);

		return new Y(slot, name, grbVar);
	}
}
