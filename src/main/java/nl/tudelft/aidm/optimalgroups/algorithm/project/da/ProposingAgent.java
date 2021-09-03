package nl.tudelft.aidm.optimalgroups.algorithm.project.da;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class ProposingAgent extends Agent
{
	public final Agent agent;
	private final Stack<Project> unproposedInDecreasingPreferentialOrder;
	private final Proposal.Template proposalTemplate;

	protected ProposingAgent(Agent agent, Proposal.Template proposalTemplate)
	{
		super(agent);

		this.agent = agent;
		this.proposalTemplate = proposalTemplate;

		var prefProjects = new ArrayList<>(agent.projectPreference().asList());
		Collections.reverse(prefProjects);

		unproposedInDecreasingPreferentialOrder = new Stack<>();
		unproposedInDecreasingPreferentialOrder.addAll(prefProjects);
	}

	public Proposal makeNextProposal()
	{
		try {
//			var utilityOfGettingProject = unproposedInDecreasingPreferentialOrder.size();
			var projectToProposeToNext = unproposedInDecreasingPreferentialOrder.pop();

			return proposalTemplate.newProposal(this, projectToProposeToNext);
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}
}
