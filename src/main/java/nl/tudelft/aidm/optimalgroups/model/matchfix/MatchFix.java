package nl.tudelft.aidm.optimalgroups.model.matchfix;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Arrays;

public record MatchFix(Group group, Project project)
{
	public static MatchFix from(Agents agents, Project project)
	{
		return new MatchFix(new FixedMatchGroup(agents, project), project);
	}
	public static MatchFix fromIds(CourseEdition dataset, Integer projectId, Integer... agentIds)
	{
		var proj = dataset.findProjectByProjectId(projectId).orElseThrow();
		
		var agents = Arrays.stream(agentIds)
				.map(agentId -> dataset.findAgentByUserId(agentId).orElseThrow())
				.collect(Agents.collector);

		var group = new FixedMatchGroup(agents, proj);
		return new MatchFix(group, proj);
	}
}
