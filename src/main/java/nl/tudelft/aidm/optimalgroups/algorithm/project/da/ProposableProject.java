package nl.tudelft.aidm.optimalgroups.algorithm.project.da;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProposableProject implements Project
{
	private final int capacity;
	public final Project project;

	private final List<TentativelyAcceptedProposal> tentativelyAccepted;

	/**
	 * @param project The underlying project
	 */
	public ProposableProject(Project project)
	{
		this.capacity = project.slots().size();
		this.tentativelyAccepted = new ArrayList<>(capacity);

		this.project = project;
	}

	public void handleProposal(Proposal proposal)
	{
		boolean notAtCapacity = tentativelyAccepted.size() < capacity;

		/* Have room - accept */
		if (notAtCapacity) {
			tentativelyAccepted.add(new TentativelyAcceptedProposal(proposal));
			proposal.tentativelyAccept();
			return;
		}

		// TODO/Idea: _expected_ utility after reject can be a heuristic - but not very strategy-proof

		/* No capacity left - decline proposal, or decline this proposing agent? */
		// Find all agents that are better off being rejected than the currently proposing agent
		var rejectableProposals = tentativelyAccepted.stream()
			.filter(tentativelyAcceptedProposal -> tentativelyAcceptedProposal.agentsExpectedUtilityAfterReject() > proposal.agentsExpectedUtilityAfterReject())
			.collect(Collectors.toList());

		if (rejectableProposals.isEmpty()) {
			// Nobody is better off being rejected than the currently proposing agent (then we apply first-come first-serve rules and the new proposal isn't the first)
			// (please disregard the contextually unfitting method name... Java8's Consumer<T>#accept(T))
			proposal.reject();
			return;
		}

		// Fairness idea: keep track of num demotions and demote the agent with least amount of demotions
		// however, in our current problem setting we already do that as the utility derived from ranks is pretty much that.

		// Now reject least-impacted agent
		rejectableProposals.stream()
			.max(Comparator.comparing(Proposal::agentsExpectedUtilityAfterReject))
			.ifPresentOrElse(proposalToReject -> {
				proposalToReject.reject();
				tentativelyAccepted.remove(proposalToReject);

				tentativelyAccepted.add(new TentativelyAcceptedProposal(proposal));
			},
			() -> {
				throw new RuntimeException("Could not find a proposal to reject but had to - check if correct");
			});

	}

	public Collection<Agent> acceptedAgents()
	{
		return tentativelyAccepted.stream().map(tentativelyAcceptedProposal -> tentativelyAcceptedProposal.proposingAgent.agent)
			.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public String name()
	{
		return project.name();
	}

	@Override
	public int sequenceNum()
	{
		return project.sequenceNum();
	}

	@Override
	public List<ProjectSlot> slots()
	{
		return project.slots();
	}

	static class TentativelyAcceptedProposal extends Proposal
	{
		public TentativelyAcceptedProposal(Proposal proposal)
		{
			super(proposal);
		}
	}

}
