package nl.tudelft.aidm.optimalgroups.algorithm.project.da;

import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class Proposal
{

	@FunctionalInterface
	public interface Template
	{
		Proposal newProposal(ProposingAgent proposingAgent, Project project);
	}

	@FunctionalInterface
	public interface AcceptFn
	{
		void tentativelyAccept(Proposal proposal);
	}

	@FunctionalInterface
	public interface RejectFn
	{
		void reject(Proposal proposal);
	}

	final ProposingAgent proposingAgent;
	private final Project project;

	private final AcceptFn proposalAcceptFn;
	private final RejectFn proposalRejectFn;

	//		final Integer utilityIfDeclined;
	private final Integer utilityOfAccepted;
	private final Integer utilityOfRejected;


	public Proposal(ProposingAgent proposingAgent, Project project, AcceptFn proposalAcceptFn, RejectFn proposalRejectFn)
	{
		this.proposingAgent = proposingAgent;
		this.project = project;

		this.proposalAcceptFn = proposalAcceptFn;
		this.proposalRejectFn = proposalRejectFn;

//		int agentsProjectRank = proposingAgent.projectPreference.rankOf(project);
		int numAllProjects = proposingAgent.projectPreference().asArray().length;
		int rankOfProjectProposingTo = proposingAgent.projectPreference().rankOf(project);
		this.utilityOfAccepted = numAllProjects - rankOfProjectProposingTo + 1; // correct for Ranks being 1-based

		this.utilityOfRejected = this.utilityOfAccepted - 1;
	}

	public Proposal(Proposal proposal)
	{
		this.proposingAgent = proposal.proposingAgent;
		this.project = proposal.project;
		this.proposalAcceptFn = proposal.proposalAcceptFn;
		this.proposalRejectFn = proposal.proposalRejectFn;
		this.utilityOfAccepted = proposal.utilityOfAccepted;
		this.utilityOfRejected = proposal.utilityOfRejected;
	}

	public Project projectProposingFor() {
		return project;
	}

	public Integer utilityIfAccepted()
	{
		return utilityOfAccepted;
	}

	public Integer agentsExpectedUtilityAfterReject()
	{
		return utilityOfRejected;
	}

	public void tentativelyAccept()
	{
		proposalAcceptFn.tentativelyAccept(this);
	}

	public void reject()
	{
		proposalRejectFn.reject(this);
	}
}
