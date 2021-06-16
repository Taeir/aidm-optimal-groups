package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints;

import gurobi.GRBException;
import gurobi.GRBModel;

public interface Constraint
{
	void apply(GRBModel model, AssignmentConstraints assignmentConstraints) throws GRBException;
}
