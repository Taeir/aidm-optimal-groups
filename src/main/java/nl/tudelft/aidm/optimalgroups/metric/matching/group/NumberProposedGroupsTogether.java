package nl.tudelft.aidm.optimalgroups.metric.matching.group;

import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;

public class NumberProposedGroupsTogether
{
	private final int asInt;
	
	public NumberProposedGroupsTogether(GroupToProjectMatching<?> matching, Groups<?> proposedGroups)
	{
		// Note that an actual group may consist of 1 or more(!) proposed groups
		var numProposedTogether = proposedGroups.asCollection().stream()
				.filter(proposed -> matching.asList().stream().anyMatch(actual -> actual.from().members().containsAll(proposed.members())))
				.count();
		
		this.asInt = (int) numProposedTogether;
	}
	
	public int asInt()
	{
		return this.asInt;
	}
}
