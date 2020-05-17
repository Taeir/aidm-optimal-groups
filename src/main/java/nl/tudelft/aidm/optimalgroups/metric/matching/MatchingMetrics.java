package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.matching.gini.GiniCoefficient;
import nl.tudelft.aidm.optimalgroups.metric.matching.gini.GiniCoefficientGroupRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.gini.GiniCoefficientStudentRank;
import nl.tudelft.aidm.optimalgroups.metric.rank.distribution.StudentRankDistributionInMatching;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCRGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.metric.rank.AvgAssignedRank;
import nl.tudelft.aidm.optimalgroups.metric.rank.WorstAssignedRank;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

/**
 * Collection of metrics available for a Matching, See the implementations
 * that are inner classes of this interface
 * @param <FROM>
 * @param <TO>
 */
public interface MatchingMetrics<FROM,TO>
{
	/**
	 * The gini coefficient of the preferences of assigning {@link FROM} to {@link TO}
	 */
	GiniCoefficient giniCoefficient();

	/**
	 * The average rank of assigned {@link TO}s in {@link FROM}s preferences
	 */
	AvgAssignedRank avgRank();

	/**
	 * The worst rank of assigned {@link TO}s in {@link FROM}s preferences
	 */
	WorstAssignedRank worstRank();

	/**
	 * The AUPCR of the matching
	 */
	AUPCR aupcr();


	/* IMPLS */
	class StudentProject implements MatchingMetrics<Agent, Project>
	{
		private final Matching<Agent, Project> matching;

		public StudentProject(Matching<Agent, Project> matching)
		{
			this.matching = matching;
		}

		@Override
		public GiniCoefficient giniCoefficient()
		{
			return new GiniCoefficientStudentRank(matching);
		}

		@Override
		public AvgAssignedRank avgRank()
		{
			return new AvgAssignedRank.AssignedProjectToAgent(matching);
		}

		@Override
		public WorstAssignedRank worstRank()
		{
			return new WorstAssignedRank.ProjectToStudents(matching);
		}

		@Override
		public AUPCR aupcr()
		{
			return new AUPCRStudent(matching);
		}

		public StudentRankDistributionInMatching rankDistribution()
		{
			return new StudentRankDistributionInMatching(matching);
		}
	}

	class GroupProject implements MatchingMetrics<Group, Project>
	{
		private final Matching<? extends Group, Project> matching;

		public GroupProject(Matching<? extends Group, Project> matching)
		{
			this.matching = matching;
		}

		@Override
		public GiniCoefficient giniCoefficient()
		{
			return new GiniCoefficientGroupRank(matching);
		}

		@Override
		public AvgAssignedRank avgRank()
		{
			return new AvgAssignedRank.AssignedProjectToGroup(matching);
		}

		@Override
		public WorstAssignedRank worstRank()
		{
			return new WorstAssignedRank.ProjectToGroup(matching);
		}

		@Override
		public AUPCR aupcr()
		{
			return new AUPCRGroup(matching);
		}
	}
}
