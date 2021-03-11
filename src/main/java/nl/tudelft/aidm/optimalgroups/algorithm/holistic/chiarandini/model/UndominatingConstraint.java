package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import edu.princeton.cs.algs4.Counter;
import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
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
public class UndominatingConstraint
{

	public UndominatingConstraint(AssignmentConstraints assignmentConstraints, GRBModel model, Profile profile, Agents undominatedAgents, SequentualProjects allProjects)
	{
		Assert.that(profile.size() == undominatedAgents.count())
			.orThrowMessage("Given profile is not fit to use with given set of agents (different sizes)");

		// Ugly because my SequentualAgents type is messy
		Assert.that(undominatedAgents.asCollection().stream().allMatch(agent -> agent instanceof SequentualAgents.SequentualAgent)).orThrowMessage("UndominatedAgents are of wrong type");


		// Copied and modified from DistributiveWeightsObjective

		var cumSumStudentsUpToRankH = new GRBLinExpr();
		var cumsumInProfileUpToRankH = new AtomicInteger(0); // needs to be effectively final for lambda's

		for (var h = new AtomicInteger(); h.get() < profile.maxRank(); h.incrementAndGet())
		{
			undominatedAgents.forEach(agent -> {
				agent.projectPreference().forEach((project, rank) -> {
					project.slots().forEach(slot ->
					{
						// Agent is not indiff and finds project acceptable
						// AND ranks project at h
						if (!rank.isCompletelyIndifferent() && !rank.unacceptable() && rank.asInt() == h.getPlain()) {
							var x = assignmentConstraints.xVars.of(agent, slot).orElseThrow();
							cumSumStudentsUpToRankH.addTerm(1, x.asVar());
						}
					});

				});
			});

			// cumsumInProfileUpToRankH += profile.numInRank(h)
			cumsumInProfileUpToRankH.addAndGet(profile.numInRank(h.getPlain()));


			// Gurobi manual: Once you add a constraint to your model, subsequent changes to the expression object you used to build the constraint will not change the constraint
			Try.doing(() ->
				model.addConstr(cumSumStudentsUpToRankH, GRB.GREATER_EQUAL, cumsumInProfileUpToRankH.getPlain(), "const_leximin_" + h.getPlain())
			).or(Rethrow.asRuntime());

		}
	}

}
