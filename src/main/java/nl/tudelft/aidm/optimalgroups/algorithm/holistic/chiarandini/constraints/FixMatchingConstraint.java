package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class FixMatchingConstraint implements Constraint
{
	private final Agent agent;
	private final Project project;
	
	public FixMatchingConstraint(Agent agent, Project project)
	{
		this.agent = agent;
		this.project = project;
	}
	
	@Override
	public void apply(GRBModel model, AssignmentConstraints assignmentConstraints) throws GRBException
	{
		// Agent must be assigned to project, each X var is for a project-slot.
		// So agent must be assigned to exactly one of the slots, which we implement by
		// taking the sum of the 0,1 decision vars of agent and corresponding slots of project, and
		// constraining that sum to be exactly 1
		
		var lhs = new GRBLinExpr();
		
		project.slots().forEach(projectSlot -> {
			var x = assignmentConstraints.xVars.of(agent, projectSlot).orElseThrow(() -> {
				throw new RuntimeException("Agent that needs fixing does not have a corresponding x-var for project slot");
			});
			
			lhs.addTerm(1, x.asVar());
		});
		
		model.addConstr(lhs, GRB.EQUAL, 1, String.format("agent%s_fixedto_proj%s", agent.id, project.id()));
	}
	
	@Override
	public String simpleName()
	{
		return "fix_matching_" + agent.toString() + "_to_" + project.name();
	}
}
