package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints;

import gurobi.*;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.GurobiHelperFns;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

/**
 * Creates the constraint to enforce "stability" as defined by Chiarandini, Fagerberg et al
 */
public record StabilityConstraint(AssignmentConstraints assignmentCnstr, SequentualDatasetContext datasetContext) implements Constraint
{
	//B[][] b, Z[][][] z, D[][][] d
	
	// Enforce stability through a constraint, in contrast to an objective function minimizing destability
	
	@Override
	public void apply(GRBModel model) throws GRBException
	{
		int numAgents = datasetContext.allAgents().count();
		int numProjs = datasetContext.allProjects().count();
		int numMaxSlots = datasetContext.numMaxSlots();
		
		// Note: proj id's are 1-based, so +1 to index by their id's
		B[][] b = new B[numProjs +1][numMaxSlots];
		
		datasetContext.allProjects().forEach(project -> {
			project.slots().forEach(slot -> {
				b[project.id()][slot.index()] = B.createInModelWithConstraint(model, assignmentCnstr, datasetContext, slot);
			});
		});
		
		Z[][][] z = new Z[numAgents+1][numProjs+1][numMaxSlots];
		
		datasetContext.allProjects().forEach(project ->
		{
			var p = project.id();
			project.slots().forEach(slot ->
			{
				var sl = slot.index();
				datasetContext.allAgents().forEach(student ->
				{
					var s = student.id;
					
					var rank = student.projectPreference().rankOf(project);
					if (rank.unacceptable() || rank.isCompletelyIndifferent())
						return;
					
					var b_sl = b[p][sl];
					z[s][p][sl] = Z.createInModelWithConstraint(model, assignmentCnstr, datasetContext, student, slot, b_sl);
				});
			});
		});
		
		D[][][] d = D.createInModelWithConstraints(model, datasetContext, assignmentCnstr.xVars, z);
		
		createStabilityFeasibilityConstraint(model, d);
	}
	
	private void createStabilityFeasibilityConstraint(GRBModel model, D[][][] d)
	{
		var sum = new GRBLinExpr();
		
		for (int k = 0; k < d.length; k++)
		{
			for (int p_j = 0; p_j < d[k].length; p_j++)
			{
				for (int slot_j = 0; slot_j < d[k][p_j].length; slot_j++)
				{
					var d_kj = d[k][p_j][slot_j];
					
					if (d_kj != null)
						sum.addTerm(1, d_kj.asVar());
				}
			}
		}
		
		Try.doing(() -> model.addConstr(sum, GRB.EQUAL, 0, "constr_stability_feasibility"))
			.or(Rethrow.asRuntime());
	}

	public record D(Agent student, Project.ProjectSlot slot, String name, GRBVar asVar)
	{
		public static D[][][] createInModelWithConstraints(GRBModel model, SequentualDatasetContext datasetContext, AssignmentConstraints.XVars xVars, Z[][][] z)
		{
			var ub = datasetContext.allProjects().count() - 1;

			int numAgents = datasetContext.allAgents().count();
			int numProjs = datasetContext.allProjects().count();
			int numMaxSlots = datasetContext.numMaxSlots();

			D[][][] d = new D[numAgents + 1][numProjs + 1][numMaxSlots];

			// d_ki >= (v_ki - v_kj)(x_ki + z_kj - 1)  forall p_j,i in agent's prefs s.t. p_i != p_j and v_kj < v_ki (a prefers j over i)

			// k
			datasetContext.allAgents().forEach(student ->
			{
				// Skip - not impacted by stability
				if (student.projectPreference().isCompletelyIndifferent())
					return;
				var k = student.id;

				// i
				datasetContext.allProjects().forEach(project_i ->
				{
					var rank_i = student.projectPreference().rankOf(project_i);
					if (rank_i.unacceptable())
						return; // Skip

					// j
					datasetContext.allProjects().forEach(project_j ->
					{
						// i != j?
						if (project_j.equals(project_i)) return;

						var rank_j = student.projectPreference().rankOf(project_j);
						if (rank_j.unacceptable()) return;

						// v_kj < v_ki?
						if (rank_i.asInt() <= rank_j.asInt()) return;

						var rankDelta = rank_i.asInt() - rank_j.asInt();

						project_i.slots().forEach(slot_i ->
						{
							var x_ki = xVars.of(student, slot_i).orElseThrow();
							var d_ki = createVariable(model, student, slot_i, ub);

							d[k][project_i.id()][slot_i.index()] = d_ki;

							project_j.slots().forEach(slot_j ->
							{
								var z_kj = z[k][project_j.id()][slot_j.index()];

								// the rhs: (v_ki - v_kj)(x_ki + z_kj - 1)
								var rhsExpr33 = new GRBLinExpr();

								// this is the right part: (x_ki + z_kj - 1)
								var rhsSubExpr = new GRBLinExpr();
								rhsSubExpr.addTerm(1, x_ki.asVar());
								rhsSubExpr.addTerm(1, z_kj.asVar());
								rhsSubExpr.addConstant(-1);

								Try.doing(() -> rhsExpr33.multAdd(rankDelta, rhsSubExpr))
									.or(Rethrow.asRuntime());

								Try.doing(() -> model.addConstr(d_ki.asVar(), GRB.GREATER_EQUAL, rhsExpr33, "constr_33_" + d_ki.name()))
									.or(Rethrow.asRuntime());
							});
						});

					});
				});
			});

			return d;
		}

		private static D createVariable(GRBModel model, Agent student, Project.ProjectSlot slot, int ub)
		{
			var name = "d_" + student + "_" + slot;
			var asVar = GurobiHelperFns.makeIntegerVariable(model, 0d, ub, name);
			return new D(student, slot, name, asVar);
		}
	}

	public record Z(Agent student, Project.ProjectSlot slot, String name, GRBVar asVar)
	{
		public static Z createInModelWithConstraint(GRBModel model, AssignmentConstraints assignmentCnstr, SequentualDatasetContext datasetContext, Agent student, Project.ProjectSlot slot, B b_sl)
		{
			int u = datasetContext.groupSizeConstraint().maxSize();
			int l = datasetContext.groupSizeConstraint().minSize();

			// create Z-var for student-project
			String z_name = "z_" + student + slot;
			var z__s_sl = GurobiHelperFns.makeBinaryVariable(model, z_name);

			// Constraint (31)
			var lhExp31 = new GRBLinExpr();
			lhExp31.addTerm(1d, b_sl.asVar());

			var rhExp31 = new GRBLinExpr();
			rhExp31.addTerm(u, z__s_sl);

			Try.doing(() -> model.addConstr(lhExp31, GRB.LESS_EQUAL, rhExp31, "constr_31_" + student + "_" + slot))
				.or(Rethrow.asRuntime());

			// Constraint (32)
			var lhExpr32 = new GRBLinExpr();
			lhExpr32.addConstant(1 - l);
			var y_sl = assignmentCnstr.yVars.of(slot).orElseThrow();
			lhExpr32.addTerm(-l, y_sl.asVar());

			var rhExpr32 = new GRBLinExpr();
			Try.doing(() -> rhExpr32.add(rhExp31)) // Rly good api...
				.or(Rethrow.asRuntime());
			rhExpr32.addTerm(u+1, y_sl.asVar());

			Try.doing(() -> model.addConstr(lhExpr32, GRB.LESS_EQUAL, rhExpr32, "constr_32_" + student + "_" + slot))
				.or(Rethrow.asRuntime());

			return new Z(student, slot, z_name, z__s_sl);
		}
	}

	public record B(Project.ProjectSlot slot, String name, GRBVar asVar)
	{
		public static B createInModelWithConstraint(GRBModel model, AssignmentConstraints assignmentCnstr, SequentualDatasetContext datasetContext, Project.ProjectSlot slot)
		{
			// Make the Var:
			var ub_sl = datasetContext.groupSizeConstraint().maxSize();
			var name = "b_" + slot.id();
			var b_slot = GurobiHelperFns.makeIntegerVariable(model, 0, ub_sl, name);

			// Make the Constraint:
			var lhLinExp = new GRBLinExpr();

			// b_sl = ub - |assigned to proj-slot sl|
			datasetContext.allAgents().forEach(agent ->
			{
				assignmentCnstr.xVars.of(agent, slot).ifPresent(x -> {
					lhLinExp.addTerm(1d, x.asVar());
				});
			});

			lhLinExp.addTerm(1d, b_slot);

			var rhLinExp = new GRBLinExpr();
			var y_sl = assignmentCnstr.yVars.of(slot).orElseThrow();
			rhLinExp.addTerm(ub_sl, y_sl.asVar());

			Try.doing(() -> model.addConstr(lhLinExp, GRB.EQUAL, rhLinExp, "b_slack_constr_" + slot.id()))
				.or(Rethrow.asRuntime());

			return new B(slot, name, b_slot);
		}
	}
}
