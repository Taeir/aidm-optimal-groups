package nl.tudelft.aidm.optimalgroups.dataset;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import static java.util.stream.Collectors.*;

public class DatasetContextWithoutPeerPrefs implements DatasetContext
{
	private final CourseEdition originalDatasetContext;
	private final Agents agents;

	public DatasetContextWithoutPeerPrefs(CourseEdition courseEdition)
	{
		this.originalDatasetContext = courseEdition;
		this.agents = withoutPeerPrefs(courseEdition.allAgents(), this);
	}

	private static Agents withoutPeerPrefs(Agents agents, DatasetContext currentContext)
	{
		return agents.asCollection().stream()
			.map(agent -> {
				var modAgent = new Agent.AgentInDatacontext(agent.sequenceNumber, agent.projectPreference(), GroupPreference.none(), currentContext);
				return (Agent) modAgent;
			})
			.collect(collectingAndThen(toList(), Agents::from));
	}

	@Override
	public String identifier()
	{
		return originalDatasetContext.identifier() + "-no_peer";
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
		return originalDatasetContext.toString() + "-no_peer";
	}
}
