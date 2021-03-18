package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints;

import gurobi.GRBException;
import gurobi.GRBModel;

public interface Constraint
{
	void apply(GRBModel model) throws GRBException;
}
