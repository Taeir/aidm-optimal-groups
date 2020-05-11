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
}
