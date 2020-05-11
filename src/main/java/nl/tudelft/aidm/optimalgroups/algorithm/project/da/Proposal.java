package nl.tudelft.aidm.optimalgroups.algorithm.project.da;

import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class Proposal
{
	final ProposingAgent proposingAgent;
	private final Project project;

	//		final Integer utilityIfDeclined;
	private final Integer utilityOfAccepted;
	private final Integer utilityOfRejected;

	public Proposal(ProposingAgent proposingAgent, Project project, Integer utilityOfAccepted)
	{
		this.proposingAgent = proposingAgent;
		this.project = project;

//		int agentsProjectRank = proposingAgent.projectPreference.rankOf(project);
		this.utilityOfAccepted = utilityOfAccepted;
		this.utilityOfRejected = utilityOfAccepted - 1;
	}

	public Proposal(Proposal proposal)
	{
		this.proposingAgent = proposal.proposingAgent;
		this.project = proposal.project;
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

	public Integer utilityIfRejected()
	{
		return utilityOfRejected;
	}

}
