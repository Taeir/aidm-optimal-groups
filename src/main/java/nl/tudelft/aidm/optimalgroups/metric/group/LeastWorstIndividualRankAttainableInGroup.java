package nl.tudelft.aidm.optimalgroups.metric.group;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.Group;

import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * <p>A group consists of multiple individuals (agents). Each having preferences over projects,
 * sometimes the individuals are closely aligned in terms of preferences, sometimes not so much.
 *
 * <p>This metric determines the best achievable worst rank. That is, when a project is assigned
 * to a group, each individual member might rank it differently. The worst rank in this case
 * is the lowest rank a member will have for that project. A best worst rank, is the highest
 * rank the least satisfied member in this group could obtain, given a free choice from all
 * projects.
 *
 */
public class LeastWorstIndividualRankAttainableInGroup
{
	private final Agents members;
	private OptionalInt bestWorstAsInt;

	public LeastWorstIndividualRankAttainableInGroup(Group group)
	{
		this.members = group.members();
	}

	public LeastWorstIndividualRankAttainableInGroup(Agents members)
	{
		this.members = members;
	}

	public OptionalInt asInt()
	{
		if (bestWorstAsInt != null) {
			return bestWorstAsInt;
		}

		var allProjects = members.asCollection().stream()
			.map(Agent::projectPreference)
			.flatMap(projectPreference -> projectPreference.asListOfProjects().stream())
			.distinct()
			.collect(Collectors.toList());

		var bestWorst = allProjects.stream()
			.mapToInt(project ->
				members.asCollection()
					.stream()
					.map(Agent::projectPreference)
					.flatMapToInt(projectPreference -> projectPreference.rankOf(project).stream())
					.max().orElse(0) // 0 if all agents turn out to be (magically) indifferent
			)
			.min();

		this.bestWorstAsInt = bestWorst;
		return this.bestWorstAsInt;
	}
}
