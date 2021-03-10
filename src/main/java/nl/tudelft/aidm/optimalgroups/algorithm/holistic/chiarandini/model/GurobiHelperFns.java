package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public class GurobiHelperFns
{
	public static GRBVar makeBinaryVariable(GRBModel model, String name)
	{
		try {
			return model.addVar(0.0, 1.0, 0.0, GRB.BINARY, name);
		}
		catch (GRBException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static GRBVar makeIntegerVariable(GRBModel model, double lb, double ub, String name)
	{
		return Try.getting(() -> model.addVar(lb, ub, 1.0, GRB.INTEGER, name))
			.or(Rethrow.asRuntime());
	}
}
