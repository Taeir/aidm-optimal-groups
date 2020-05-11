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

	protected ProposingAgent(Agent agent)
	{
		super(agent);

		this.agent = agent;

		var prefProjects = new ArrayList<>(agent.projectPreference().asListOfProjects());
		Collections.reverse(prefProjects);

		unproposedInDecreasingPreferentialOrder = new Stack<>();
		unproposedInDecreasingPreferentialOrder.addAll(prefProjects);
	}

	public Proposal makeNextProposal()
	{
		try {
			var utilityOfGettingProject = unproposedInDecreasingPreferentialOrder.size();
			var projectToProposeToNext = unproposedInDecreasingPreferentialOrder.pop();

			return new Proposal(this, projectToProposeToNext, utilityOfGettingProject);
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}
}
