package nl.tudelft.aidm.optimalgroups.metric.matching.group;

import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

public class NumberProposedGroupsTogether
{
	private final int asInt;
	
	public NumberProposedGroupsTogether(GroupToProjectMatching<?> matching, Groups<?> proposed)
	{
		this.asInt = (int) matching.asList().stream()
			    // Filter out any groups that are not a superset of one of the given groups
				.filter(match -> proposed.asCollection().stream().anyMatch(givenGroup -> match.from().members().containsAll(givenGroup.members())))
				.count();
	}
	
	public int asInt()
	{
		return this.asInt;
	}
}
