package nl.tudelft.aidm.optimalgroups.algorithm.generic.da;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.*;
import java.util.function.Function;

public class Mechanism<PROPOSER, PROPOSED>
{
	private final List<Proposer<PROPOSER, PROPOSED>> proposers;
	private final Proposables<PROPOSER, PROPOSED> proposables;

	private List<Match<Agent, Project>> matchingOutcome;

	public Mechanism(List<Proposer<PROPOSER, PROPOSED>> proposers, Proposables<PROPOSER, PROPOSED> proposables)
	{
		this.proposers = proposers;
		this.proposables = proposables;
	}

	public List<Match<PROPOSER, PROPOSED>> determine()
	{
		var unmatched = new Stack<Proposer<PROPOSER, PROPOSED>>();
		// everybody starts unmatched
		unmatched.addAll(proposers);

		// rejection means re-add to unmatched
		Proposal.Actionable.RejectFn<PROPOSER, PROPOSED> rejectionFn = proposal -> {
//			System.out.printf("   Student %s rejected\n", stud.id);
			var proposerable = proposers.stream()
				.filter(p -> p.subject().equals(proposal.proposer()))
				.findAny().orElseThrow();
			unmatched.add(proposerable);
		};

		Function<Proposal.Actionable<PROPOSER, PROPOSED>, Proposal.Actionable<PROPOSER, PROPOSED>> proposalTemplate = proposal -> new Proposal.Actionable<>()
			{
				@Override
				public void tentativelyAccept()
				{
				}

				@Override
				public void reject()
				{
					rejectionFn.reject(proposal());
				}

				@Override
				public Proposal<PROPOSER, PROPOSED> proposal()
				{
					return proposal;
				}
			};

//			(proposingAgent, project) ->
//			new Proposal(proposingAgent, project,
//				proposal -> {}, // do nothing if accepted (ProposableProjects manage their tentatively accepted)
//				proposal -> rejectionFn.accept(proposal.proposerable)
//			);

		while (unmatched.size() > 0) {
			var unmatchedProposer = unmatched.pop();
			var proposal = unmatchedProposer.makeNextProposal();
//			System.out.printf("Student %s,\tproposing to: %s\n", unmatchedProposer.agent, proposal.projectProposingFor().id());
			proposables.receiveProposal(proposalTemplate.apply(proposal));
		}

		/* Algo done, now transform into a Matching */
		List<Match<PROPOSER, PROPOSED>> matching = new ArrayList<>();
		for (var proposable : proposables)
		{
			proposable.accepted().forEach(proposer -> {
				var match = new Match<PROPOSER, PROPOSED>()
				{
					@Override
					public PROPOSER from()
					{
						return proposer;
					}

					@Override
					public PROPOSED to()
					{
						return proposable.underlying();
					}
				};

				matching.add(match);
			});
		}

		return matching;
	}

}
