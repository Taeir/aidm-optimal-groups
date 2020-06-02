package nl.tudelft.aidm.optimalgroups.algorithm.group.partial;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.pref.AggregatedProfilePreference;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class GroupsFromCliques extends Groups.ListBacked<Group.TentativeGroup>
{
	private final Agents agents;
	private List<Group.TentativeGroup> tentativeCliques;

	public GroupsFromCliques(Agents agents)
	{
		this.agents = agents;
	}

	@Override
	protected List<Group.TentativeGroup> asList()
	{
		if (tentativeCliques == null) {
			tentativeCliques = cliquesExtractedFrom(agents, agents.datsetContext.groupSizeConstraint());
		}

		return tentativeCliques;
	}

	private static List<Group.TentativeGroup> cliquesExtractedFrom(Agents agents, GroupSizeConstraint groupSizeConstraint)
	{
		var available = new HashSet<>(agents.asCollection());
		var unavailable = new HashSet<Agent>(available.size());

		var tentativelyFormed = new LinkedList<Group.TentativeGroup>();

		// Note: we cannot simply iterate over the "available" students because we need to modify that collection during
		// each iteration - such action results in a ConcurrentModificationException in Java. Therefore, we iterate over
		// agents.asCollection (which are the same agents that are 'available' at the start of the execution of the function
		// and simply skip already processed agents
		for (Agent student : agents.asCollection())
		{
			if (unavailable.contains(student)) {
				// already processed this guy, next!
				continue;
			}

			if (student.groupPreferenceLength() > groupSizeConstraint.maxSize()) {
				// Don't allow cliques larger than max group size
				continue;
			}

			if (student.groupProposalIsMutual()) {
				var peers = student.groupPreference.asList();
				var proposedGroup = Agents.from(student).with(peers);

				var tentativeGroup = new Group.TentativeGroup(proposedGroup, AggregatedProfilePreference.usingGloballyConfiguredMethod(proposedGroup));
				tentativelyFormed.add(tentativeGroup);

				available.removeAll(tentativeGroup.members().asCollection());
				unavailable.addAll(tentativeGroup.members().asCollection());
			}
		}

		return tentativelyFormed;
	}
}
