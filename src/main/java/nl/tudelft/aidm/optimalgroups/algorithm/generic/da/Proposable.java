package nl.tudelft.aidm.optimalgroups.algorithm.generic.da;

import java.util.*;
import java.util.stream.Collectors;

public interface Proposable<PROPOSER, PROPOSED>
{
	public enum ProposalAnswer { TentivelyAccept, Reject };

	public ProposalAnswer handleProposal(Proposal.Actionable<PROPOSER, PROPOSED> proposal);
	public Collection<PROPOSER> accepted();
	public PROPOSED underlying();

	class Generic<PROPOSER, PROPOSED> implements Proposable<PROPOSER, PROPOSED>
	{
		private final int capacity;
		public final PROPOSED theProposable;

		private final List<Proposal.Actionable<PROPOSER, PROPOSED>> tentativelyAccepted;

		/**
		 * @param theProposed The object that is proposable - that is, to be made proposable
		 */
		public Generic(PROPOSED theProposed, int capacity)
		{
			this.capacity = capacity;
			this.tentativelyAccepted = new ArrayList<>(capacity);

			this.theProposable = theProposed;
		}

		public ProposalAnswer handleProposal(Proposal.Actionable<PROPOSER, PROPOSED> proposal)
		{
			boolean notAtCapacity = tentativelyAccepted.size() < capacity;

			/* Have room - accept */
			if (notAtCapacity) {
				tentativelyAccepted.add(proposal);
				proposal.tentativelyAccept();
				return ProposalAnswer.TentivelyAccept;
			}
			// TODO/Idea: _expected_ utility after reject can be a heuristic - but not very strategy-proof

			/* No capacity left - decline proposal, or decline this proposing agent? */
			// Find all agents that are better off being rejected than the currently proposing agent
			var rejectableProposals = tentativelyAccepted.stream()
//				.filter(tentativelyAcceptedProposal -> tentativelyAcceptedProposal.agentsExpectedUtilityAfterReject() > proposal.proposal().agentsExpectedUtilityAfterReject())
				.collect(Collectors.toList());

			if (rejectableProposals.isEmpty()) {
				// Nobody is better off being rejected than the currently proposing agent (then we apply first-come first-serve rules and the new proposal isn't the first)
				// (please disregard the contextually unfitting method name... Java8's Consumer<T>#accept(T))
				proposal.reject();
				return ProposalAnswer.Reject;
			}

			// Fairness idea: keep track of num demotions and demote the agent with least amount of demotions
			// however, in our current problem setting we already do that as the utility derived from ranks is pretty much that.

			// Now determine & reject least-impacted agent
//			var leastWorstOffProposalAfterReject = rejectableProposals.stream()
//				.max(Comparator.comparing(actionableTentative -> actionableTentative.proposal().agentsExpectedUtilityAfterReject()))
//				.orElseThrow(() -> new RuntimeException("Could not find a proposal to reject but had to - check if correct"));

//			leastWorstOffProposalAfterReject.reject();
//			tentativelyAccepted.remove(leastWorstOffProposalAfterReject);

			tentativelyAccepted.add(proposal);
			throw new RuntimeException("Fixme");

		}

		public Collection<PROPOSER> accepted()
		{
			return tentativelyAccepted.stream()
				.map(tentativelyAcceptedProposal -> tentativelyAcceptedProposal.proposer())
				.collect(Collectors.toUnmodifiableList());
		}

		public PROPOSED underlying()
		{
			return theProposable;
		}
	}
}
