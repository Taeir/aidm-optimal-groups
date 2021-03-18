package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import gurobi.GRBModel;

public interface ObjectiveFunction
{
	void apply(GRBModel model);
}
