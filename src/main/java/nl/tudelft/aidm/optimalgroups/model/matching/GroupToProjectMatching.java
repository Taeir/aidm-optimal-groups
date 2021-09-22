package nl.tudelft.aidm.optimalgroups.model.matching;

import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public interface GroupToProjectMatching<G extends Group> extends Matching<G, Project>
{
//	@Override
//	default GiniCoefficient giniCoefficient()
//	{
//		return new GiniCoefficientGroupRank(this);
//	}
//
//	@Override
//	default AvgRank avgRank()
//	{
//		return new AvgRankAssignedProjectToGroup(this);
//	}
//
//	@Override
//	default WorstRank worstRank()
//	{
//		return new WorstRankAssignedProjectToGroup(this);
//	}
	
	static GroupToProjectMatching<Group.FormedGroup> byTriviallyPartitioning(AgentToProjectMatching agentToProjectMatching)
	{
		return FormedGroupToProjectMatching.byTriviallyPartitioning(agentToProjectMatching);
	}
	
	/**
	 * Filters the matching by groups that are supersets of the given groups
	 * @param groups The groups to filter by (subsets)
	 * @return A matching holding holding only groups that are (super)sets of the given groups
	 */
	default GroupToProjectMatching<G> filteredBySubsets(Groups<?> groups)
	{
		var filteredMatches = this.asList().stream()
			    // Filter out any groups that are not a superset of one of the given groups
				.filter(match -> groups.asCollection().stream().anyMatch(givenGroup -> match.from().members().containsAll(givenGroup.members())))
				.collect(Collectors.toList());
		
		return new GroupToProjectMatching<G>()
		{
			@Override
			public List<Match<G, Project>> asList()
			{
				return filteredMatches;
			}
			
			@Override
			public DatasetContext datasetContext()
			{
				return GroupToProjectMatching.this.datasetContext();
			}
		};
	}
}
