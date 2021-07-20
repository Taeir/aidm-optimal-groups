package nl.tudelft.aidm.optimalgroups.model.matchfix;

import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.Arrays;
import java.util.stream.Collectors;

public record MatchFix(Group group, Project project)
{
	public static MatchFix from(Agents agents, Project project)
	{
		return new MatchFix(new FixedMatchGroup(agents, project), project);
	}
	public static MatchFix fromIds(DatasetContext dataset, Integer projectId, Integer... agentIds)
	{
		Assert.that(!(dataset instanceof SequentualDatasetContext)).orThrowMessage("must be og dataset");

		var proj = dataset.allProjects().findWithId(projectId).orElseThrow();

		var agents = Arrays.stream(agentIds).map(id -> dataset.allAgents().findByAgentId(id).orElseThrow())
			.collect(Collectors.collectingAndThen(Collectors.toList(), Agents::from));

		var group = new FixedMatchGroup(agents, proj);
		return new MatchFix(group, proj);
	}
}
