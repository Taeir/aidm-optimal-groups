package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Profile;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualAgents;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualProjects;
import plouchtch.assertion.Assert;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Constraint that the solution for (a subset of) students must not be dominated by the given profile
 */
public class UndominatedByProfileConstraint implements Constraint
{
	private final AssignmentConstraints assignmentConstraints;
	private final Profile profile;
	private final Agents undominatedAgents;
	private final SequentualProjects allProjects;
	
	public UndominatedByProfileConstraint(AssignmentConstraints assignmentConstraints, Profile profile, Agents undominatedAgents, SequentualProjects allProjects)
	{
		Assert.that(profile.size() == undominatedAgents.count())
			.orThrowMessage("Given profile is not fit to use with given set of agents (different sizes)");

		// Ugly because my SequentualAgents type is messy
		Assert.that(undominatedAgents.asCollection().stream().allMatch(agent -> agent instanceof SequentualAgents.SequentualAgent))
			.orThrowMessage("UndominatedAgents are of wrong type");
		
		this.assignmentConstraints = assignmentConstraints;
		this.profile = profile;
		this.undominatedAgents = undominatedAgents;
		this.allProjects = allProjects;
	}
	
	
	@Override
	public void apply(GRBModel model) throws GRBException
	{
		// Copied and modified from DistributiveWeightsObjective
		
		var cumsumInProfileUpToRankH = new AtomicInteger(0); // needs to be effectively final for lambda's
		
		var cumSumStudentsUpToRankH = new GRBLinExpr();
		
		for (var i = new AtomicInteger(); i.get() < profile.maxRank(); i.incrementAndGet())
		{
			int h = i.getPlain();
			
			undominatedAgents.forEach(agent -> {
				agent.projectPreference().forEach((project, rank) -> {
					project.slots().forEach(slot ->
					{
						// Agent is not indiff and finds project acceptable
						// AND ranks project at h
						if (!rank.isCompletelyIndifferent() && !rank.unacceptable() && rank.asInt() == h) {
							var x = assignmentConstraints.xVars.of(agent, slot).orElseThrow();
							cumSumStudentsUpToRankH.addTerm(1, x.asVar());
						}
					});
					
				});
			});
			
			// cumsumInProfileUpToRankH += profile.numInRank(h)
			var with_h_in_profile = profile.numInRank(h);
			cumsumInProfileUpToRankH.addAndGet(with_h_in_profile);
			
			
			// Gurobi manual: Once you add a constraint to your model, subsequent changes to the expression object you used to build the constraint will not change the constraint
			Try.doing(() ->
				          model.addConstr(cumSumStudentsUpToRankH, GRB.GREATER_EQUAL, cumsumInProfileUpToRankH.getPlain(), "const_leximin_" + h)
			).or(Rethrow.asRuntime());
		}
	}
}
