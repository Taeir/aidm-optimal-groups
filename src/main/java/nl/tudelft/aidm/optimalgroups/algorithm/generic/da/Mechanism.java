package nl.tudelft.aidm.optimalgroups.algorithm.generic.da;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.jetbrains.annotations.NotNull;

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

		Function<Proposal.Actionable<PROPOSER, PROPOSED>, Proposal.Actionable<PROPOSER, PROPOSED>> proposalTemplate =
			injectMechanismAcceptRejectFunctions(unmatched, rejectionFn);

//			(proposingAgent, project) ->
//			new Proposal(proposingAgent, project,
//				proposal -> {}, // do nothing if accepted (ProposableProjects manage their tentatively accepted)
//				proposal -> rejectionFn.accept(proposal.proposerable)
//			);

		// PHASE 1: ensure no single agents
		while (unmatched.size() > 0) {
			var unmatchedProposer = unmatched.pop();
			var proposal = (Proposal.Actionable<PROPOSER, PROPOSED>) unmatchedProposer.makeNextProposal();
//			System.out.printf("Student %s,\tproposing to: %s\n", unmatchedProposer.agent, proposal.projectProposingFor().id());
			var answer = proposables.receiveProposal(proposalTemplate.apply(proposal));
			switch (answer) {
				case TentivelyAccept:
			}
		}

		// PHASE 2: ensure all pairs and groups are merged into valid groups

		// Convert to a simple matching
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

	@NotNull
	private Function<Proposal.Actionable<PROPOSER, PROPOSED>, Proposal.Actionable<PROPOSER, PROPOSED>> injectMechanismAcceptRejectFunctions(Stack<Proposer<PROPOSER, PROPOSED>> unmatched, Proposal.Actionable.RejectFn<PROPOSER, PROPOSED> rejectionFn)
	{
		return proposal -> new Proposal.Actionable<>()
				{
					@Override
					public void tentativelyAccept()
					{
						unmatched.removeIf(unmatched1 -> unmatched1.subject().equals(proposal.proposer()));
						unmatched.removeIf(unmatched1 -> unmatched1.subject().equals(proposal.recipient()));
						proposal.tentativelyAccept();
					}

					@Override
					public void reject()
					{
						rejectionFn.reject(proposal);
						proposal.reject();
					}

					@Override
					public PROPOSER proposer()
					{
						return proposal.proposer();
					}

					@Override
					public PROPOSED recipient()
					{
						return proposal.recipient();
					}

//					@Override
//					public Integer utilityIfAccepted()
//					{
//						return proposal.utilityIfAccepted();
//					}
//
//					@Override
//					public Integer agentsExpectedUtilityAfterReject()
//					{
//						return proposal.agentsExpectedUtilityAfterReject();
//					}
				};
	}


}
