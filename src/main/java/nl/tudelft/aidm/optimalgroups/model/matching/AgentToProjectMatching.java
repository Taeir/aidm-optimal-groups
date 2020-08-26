package nl.tudelft.aidm.optimalgroups.model.matching;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public interface AgentToProjectMatching extends Matching<Agent, Project>
{
	static AgentToProjectMatching from(Matching<? extends Group, Project> groupMatching)
	{
		return new AgentPerspectiveGroupProjectMatching(groupMatching);
	}

	/**
	 * <p>Provides a default, cacheless implementation of groupedByProject.</p>
	 * <p>The implementation is advised to create its own implementation that
	 * either pre-computes this or caches the result of this base method.</p>
	 * @return A mappting of Agents assigned to the given Project
	 */
	default Map<Project, List<Agent>> groupedByProject()
	{
		return this.asList().stream()
			.map(AgentToProjectMatch::from)
			.collect(
				groupingBy(AgentToProjectMatch::project,
					IdentityHashMap::new, // type of map
					mapping(AgentToProjectMatch::agent, toList())
				)
			);
	}

	default int countDistinctStudents()
	{
		return (int) this.asList().stream().map(Match::from).distinct().count();
	}

//	@Override
//	default GiniCoefficient giniCoefficient()
//	{
//		return new GiniCoefficientStudentRank(this);
//	}
//
//	@Override
//	default AvgRank avgRank()
//	{
//		return new AvgRankAssignedProjectToStudent(this);
//	}
//
//	@Override
//	default WorstRank worstRank()
//	{
//		return new WorstRankAssignedProjectToStudents(this);
//	}
}
