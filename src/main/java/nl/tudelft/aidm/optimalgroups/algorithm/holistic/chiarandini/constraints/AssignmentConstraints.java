package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints;

import gurobi.*;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.XVars;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.YVars;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.*;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

import java.util.Optional;

public class AssignmentConstraints
{
	private final SequentualDatasetContext datasetContext;
	
	public final XVars xVars;
	public final YVars yVars;

	private AssignmentConstraints(SequentualDatasetContext datasetContext, XVars x, YVars y)
	{
		this.datasetContext = datasetContext;
		this.xVars = x;
		this.yVars = y;
	}

	public static AssignmentConstraints createInModel(GRBModel model, SequentualDatasetContext datasetContext) throws GRBException
	{
		var agents = datasetContext.allAgents();
		var projects = datasetContext.allProjects();

		// num slots per projects (the max)
		var SL = datasetContext.numMaxSlots();

		XVars xVars = XVars.createInModel(model, datasetContext);
		YVars yVars = YVars.createInModel(model, datasetContext);

		// CONSTRAINTS

		// - student is assigned to exactly one project
		agents.forEach(agent ->
		{
			var numSlotsAssignedToAgentExpr = new GRBLinExpr();
			var s = agent.id;

			projects.forEach(project ->
			{
				project.slots().forEach(slot ->
				{
					xVars.of(agent, slot).ifPresent(x -> {
						numSlotsAssignedToAgentExpr.addTerm(1, x.asVar());
					});
				});
			});

			String name = "s_" + agent + "assignedToOneProjCnstr";
			Try.doing(() -> model.addConstr(numSlotsAssignedToAgentExpr, GRB.EQUAL, 1, name))
				.or(Rethrow.asRuntime());
		});

		// - project is either closed or adheres to it's occupation size requirements:
		projects.forEach(project ->
		{
			var p = project.id();

			project.slots().forEach(slot ->
			{
				var sl = slot.index();
				var numStudentsAssignedToSlot = new GRBLinExpr();

				agents.forEach(agent ->
				{
					if (agent.projectPreference().rankOf(project).unacceptable()) {
						// Agent might not find this project acceptible, therefore no corresponding
						// x-variable will exist. So skip here after checking for the case, instead of
						// simply checking if the x-variable exists via the optional.isPresent check
						return;
					}
					
					var x = xVars.of(agent, slot).orElseThrow();
					numStudentsAssignedToSlot.addTerm(1, x.asVar());
				});

				var y = yVars.of(slot).orElseThrow();

				var ub = new GRBLinExpr();
				ub.addTerm(datasetContext.groupSizeConstraint().maxSize(), y.asVar());
				var constraintNameUb = String.format("proj_%s_doesnt_exceed_ub", p);

				var lb = new GRBLinExpr();
				lb.addTerm(datasetContext.groupSizeConstraint().minSize(), y.asVar());
				var constraintNameLb = String.format("proj_%s_meets_lb", p);

				// numAssignedToSlot <= UB * [0,1]
				Try.doing(() -> model.addConstr(numStudentsAssignedToSlot, GRB.LESS_EQUAL, ub, constraintNameUb))
					.or(Rethrow.asRuntime());

				// numAssignedToSlot >= LB * [0,1]
				Try.doing(() -> model.addConstr(numStudentsAssignedToSlot, GRB.GREATER_EQUAL, lb, constraintNameLb))
					.or(Rethrow.asRuntime());
			});
		});

		return new AssignmentConstraints(datasetContext, xVars, yVars);
	}

}
