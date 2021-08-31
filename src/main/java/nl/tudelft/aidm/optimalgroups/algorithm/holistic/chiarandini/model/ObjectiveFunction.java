package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;

public interface ObjectiveFunction
{
	void apply(GRBModel model, AssignmentConstraints assignmentConstraints);
	
	String name();
}
