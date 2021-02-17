package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.*;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

class AssignmentVariablesAndConstraints
{
	public final X[][][] x;
	public final Y[][] y;
	
	public static AssignmentVariablesAndConstraints createInModel(GRBModel model, SequentualDatasetContext datasetContext) throws GRBException
	{
		// num agents
		var S = datasetContext.allAgents().count();

		// num projects
		var P = datasetContext.allProjects().count();

		// num slots per projects (the max)
		var SL = datasetContext.allProjects().asCollection().stream().map(proj -> proj.slots().size()).max(Integer::compareTo)
			.orElseThrow(() -> new RuntimeException(String.format("None of the projects in dataset [%s] have slots. Bug?", datasetContext)));

		X[][][] x = new X[S+1][P+1][SL];
		Y[][] y = new Y[P+1][SL];

		// Create X variables (student assigned to slot)
		datasetContext.allAgents().forEach(student ->
		{
			var s = student.id;

			// hacky: seqentualization does properly handles the varios pref profile types, so workaround
			if (student.projectPreference().isCompletelyIndifferent())
			{
				datasetContext.allProjects().forEach(project ->
				{
					var p = project.id();
					project.slots().forEach(slot -> {
						int sl = slot.index();
						x[s][p][sl] = X.createInModel(student, project, slot, model);
					});
				});
			}
			else student.projectPreference().forEach((project, rank) ->
			{
				var p = project.id();
				project.slots().forEach(slot ->
				{
					int sl = slot.index();
					x[s][p][sl] = X.createInModel(student, project, slot, model);
				});
			});
		});

		// Create Y variables (project-slot is open)
		datasetContext.allProjects().forEach(project ->
		{
			int p = project.id();
			project.slots().forEach(slot -> {
				var sl = slot.index();
				y[p][sl] = Y.createInModel(slot, model);
			});
		});

		// CONSTRAINTS

		// - student is assigned to exactly one project
		datasetContext.allAgents().forEach(agent ->
		{
			var numSlotsAssignedToAgentExpr = new GRBLinExpr();
			var s = agent.id;

			datasetContext.allProjects().forEach(project ->
			{
				var p = project.id();
				project.slots().forEach(slot ->
				{
					if (x[s][p][slot.index()] != null)
						numSlotsAssignedToAgentExpr.addTerm(1, x[s][p][slot.index()].asVar());
				});
			});

			String name = "s_" + s + "assignedToOneProjCnstr";
			Try.doing(() -> model.addConstr(numSlotsAssignedToAgentExpr, GRB.EQUAL, 1, name))
				.or(Rethrow.asRuntime());
		});

		// - project is either closed or adheres to it's occupation size requirements:
		datasetContext.allProjects().forEach(project ->
		{
			var p = project.id();

			project.slots().forEach(slot ->
			{
				var sl = slot.index();
				var numStudentsAssignedToSlot = new GRBLinExpr();

				datasetContext.allAgents().forEach(agent ->
				{
					var s = agent.id;

					var rank = agent.projectPreference().rankOf(project);
					if (rank.unacceptable())
						return;

					numStudentsAssignedToSlot.addTerm(1, x[s][p][sl].asVar());
				});

				var ub = new GRBLinExpr();
				ub.addTerm(datasetContext.groupSizeConstraint().maxSize(), y[p][sl].asVar());
				var constraintNameUb = String.format("proj_%s_doesnt_exceed_up", p);

				var lb = new GRBLinExpr();
				lb.addTerm(datasetContext.groupSizeConstraint().minSize(), y[p][sl].asVar());
				var constraintNameLb = String.format("proj_%s_meets_lb", p);

				// numAssignedToSlot <= UB * [0,1]
				Try.doing(() -> model.addConstr(numStudentsAssignedToSlot, GRB.LESS_EQUAL, ub, constraintNameUb))
					.or(Rethrow.asRuntime());

				// numAssignedToSlot >= LB * [0,1]
				Try.doing(() -> model.addConstr(numStudentsAssignedToSlot, GRB.GREATER_EQUAL, lb, constraintNameLb))
					.or(Rethrow.asRuntime());
			});
		});

		return new AssignmentVariablesAndConstraints(datasetContext, model, x, y);
	}

	public AssignmentVariablesAndConstraints(SequentualDatasetContext datasetContext, GRBModel model, X[][][] x, Y[][] y)
	{
		this.x = x;
		this.y = y;
	}

	public X x(Agent agent, Project project, Project.ProjectSlot slot) {
		return x[agent.id][project.id()][slot.index()];
	}


	public record X(Agent student, Project project, Project.ProjectSlot slot, String name, GRBVar asVar)
	{
		public static X createInModel(Agent student, Project project, Project.ProjectSlot slot, GRBModel model)
		{
			var name = String.format("x[%s, %s]", student, slot);
			var grbVar = binaryVariable(model, name);

			return new X(student, project, slot, name, grbVar);
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	public record Y(Project.ProjectSlot project, String name, GRBVar asVar)
	{
		public static Y createInModel(Project.ProjectSlot slot, GRBModel model)
		{
			var name = String.format("y[%s]", slot.toString());
			var grbVar = binaryVariable(model, name);

			return new Y(slot, name, grbVar);
		}
	}

	public static GRBVar binaryVariable(GRBModel model, String name)
	{
		return Try.getting(() -> model.addVar(0.0, 1.0, 0.0, GRB.BINARY, name))
			.or(Rethrow.asRuntime());
	}


}
