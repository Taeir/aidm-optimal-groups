package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints;

import gurobi.*;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.GurobiHelperFns;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Model;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import plouchtch.assertion.Assert;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SoftGroupConstraint implements Constraint
{
	private final Collection<AllowGrpDecisionVar> allowGrpDecisionVars;
	private final Groups<?> groups;
	
	public SoftGroupConstraint(Groups<?> groups)
	{
		this.groups = groups;
		allowGrpDecisionVars = new ArrayList<>();
	}
	
	@Override
	public void apply(GRBModel model, AssignmentConstraints assignmentConstraints) throws GRBException
	{
		var grpIdx = new AtomicInteger(1);
		
		groups.forEach(group -> {
			var projPrefs = group.projectPreference();
			var agents = new ArrayList<>(group.members().asCollection());
			
			// transitory constraints, if Xa = Xb and Xb = Xc and Xc = Xd imply Xa = Xd, Xa = Xc etc
			//   so we set contraints that first student in group must have same assignment as the second,
			//   the second same as the third etc. This suffices to ensure that all these students in a group
			//   get the same assignment.
			
			// let this be 'g'
			var allowGrpDecisionVar = AllowGrpDecisionVar.make(group, grpIdx.getAndIncrement(), model);
				
			projPrefs.forEach(((project, rank) -> {
				project.slots().forEach(slot -> {
					
					// the x's (assignment decision var) of all agents in group for assignment to project slot 'slot'
					var xToSlotVarsAgents = agents.stream().map(agent -> assignmentConstraints.xVars.of(agent, slot))
						.flatMap(Optional::stream)
						.map(AssignmentConstraints.X::asVar)
						.collect(Collectors.toList());
					
					Try.doing(() -> {
						    var lhs = new GRBLinExpr();
							xToSlotVarsAgents.forEach(x -> lhs.addTerm(1.0, x));
							// g -> x_1 + x_2 + x_3 ... x_n == n | where x is the decision var of assigning agent to slot (context)
							model.addGenConstrIndicator(allowGrpDecisionVar.asVar(), 1, lhs, GRB.EQUAL, agents.size(), "cnstr_" + allowGrpDecisionVar.name + slot.id());
					}).or(Rethrow.asRuntime());
					
				});
			}));
		});
		
		var objective = (GRBLinExpr) model.getObjective();
		
		Assert.that(objective.size() > 0).orThrowMessage("Objective function must be set before adding the Soft Grouping constraint");
		
		allowGrpDecisionVars.forEach(allowGrpVar -> {
			// Ensure coefficient is sufficiently small that the grouping of all agents is never at the expense of a rank
			objective.addTerm(-1.0 / (groups.count() * 10), allowGrpVar.asVar()) ;
		});
	}
	
	record AllowGrpDecisionVar(GRBVar softGrpVar, String name)
	{
		static AllowGrpDecisionVar make(Group grp, Integer grpIdx, GRBModel model)
		{
			String name = "allow_g" + grpIdx;
			var softGrpVar = GurobiHelperFns.makeBinaryVariable(model, name);
			return new AllowGrpDecisionVar(softGrpVar, name);
		}
		
		public GRBVar asVar()
		{
			return this.softGrpVar;
		}
	}
}
