package nl.tudelft.aidm.optimalgroups.model.matching;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class AgentPerspectiveGroupProjectMatching extends ListBasedMatching<Agent, Project> implements AgentToProjectMatching
{
	public AgentPerspectiveGroupProjectMatching(Matching<? extends Group, Project> groupPerspectiveMatching)
	{
		super(groupPerspectiveMatching.datasetContext(),
			agentToProjectMatchListOf(groupPerspectiveMatching)
		);
	}

	@NotNull
	private static List<Match<Agent, Project>> agentToProjectMatchListOf(Matching<? extends Group, Project> groupPerspectiveMatching)
	{
		return groupPerspectiveMatching.asList().stream()
			.flatMap(groupToProjectMatch -> groupToProjectMatch.from().members().asCollection().stream().map(agent -> new AgentToProjectMatch(agent, groupToProjectMatch.to())))
			.collect(Collectors.toList());
	}
}
