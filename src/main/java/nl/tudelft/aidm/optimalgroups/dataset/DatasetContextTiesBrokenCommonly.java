package nl.tudelft.aidm.optimalgroups.dataset;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.agent.SimpleAgent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.complete.ProjectPreferenceAugmentedWithMissingAlternativesCmmnRnd;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class DatasetContextTiesBrokenCommonly implements DatasetContext
{
	private final DatasetContext originalDatasetContext;

	private final Agents agents;

	public static DatasetContext from(DatasetContext datasetContext)
	{
		return new DatasetContextTiesBrokenCommonly(datasetContext);
	}
	protected DatasetContextTiesBrokenCommonly(DatasetContext datasetContext)
	{
		this.originalDatasetContext = datasetContext;
		this.agents = tiesBrokenCommonly(datasetContext.allAgents(), datasetContext.allProjects(), this);
	}

	private static Agents tiesBrokenCommonly(Agents agents, Projects projects, DatasetContext currentContext)
	{
		var projectsShuffled = new ArrayList<>(projects.asCollection());
		Collections.shuffle(projectsShuffled);

		var agentsWithTiesBroken = agents.asCollection().stream()
			.map(agent -> {
				var origPrefs = agent.projectPreference();
				var newPrefs = new ProjectPreferenceAugmentedWithMissingAlternativesCmmnRnd(origPrefs, projects);
				return (Agent) new SimpleAgent.AgentInDatacontext(agent.sequenceNumber(), newPrefs, agent.groupPreference(), currentContext);
			})
			.collect(Collectors.collectingAndThen(Collectors.toUnmodifiableList(), Agents::from));

		return agentsWithTiesBroken;
	}

	@Override
	public String identifier()
	{
		return originalDatasetContext.identifier() + "-ties_broken_comm";
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
		return originalDatasetContext.toString() + "-ties_broken_comm";
	}
}