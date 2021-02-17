package nl.tudelft.aidm.optimalgroups.dataset;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.complete.ProjectPreferenceAugmentedWithMissingAlternativesIndvdRnd;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.stream.Collectors;

public class DatasetContextTiesBrokenIndividually implements DatasetContext
{
	private final DatasetContext originalDatasetContext;

	private final Agents agents;

	public static DatasetContext from(DatasetContext datasetContext)
	{
		return new DatasetContextTiesBrokenIndividually(datasetContext);
	}
	protected DatasetContextTiesBrokenIndividually(DatasetContext datasetContext)
	{
		this.originalDatasetContext = datasetContext;
		this.agents = tiesBrokenIndividually(datasetContext.allAgents(), datasetContext.allProjects(), this);
	}

	private static Agents tiesBrokenIndividually(Agents agents, Projects projects, DatasetContext currentContext)
	{
		var agentsWithTiesBroken = agents.asCollection().stream()
			.map(agent -> {
				var origPrefs = agent.projectPreference();
				var newPrefs = new ProjectPreferenceAugmentedWithMissingAlternativesIndvdRnd(origPrefs, projects, agent.id * projects.hashCode());
				return (Agent) new Agent.AgentInDatacontext(agent.id, newPrefs, agent.groupPreference, currentContext);
			})
			.collect(Collectors.collectingAndThen(Collectors.toUnmodifiableList(), Agents::from));

		return agentsWithTiesBroken;
	}

	@Override
	public String identifier()
	{
		return originalDatasetContext.identifier() + "-ties_broken_indiv";
	}

	@Override
	public Projects allProjects()
	{
		return originalDatasetContext.allProjects();
	}

	@Override
	public Agents allAgents()
	{
		return agents;
	}

	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		return originalDatasetContext.groupSizeConstraint();
	}

	@Override
	public String toString()
	{
		return originalDatasetContext.toString() + "-ties_broken_indiv";
	}
}
