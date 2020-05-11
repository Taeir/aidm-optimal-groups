package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.bla.AvgRank;
import nl.tudelft.aidm.optimalgroups.metric.bla.GiniCoefficient;
import nl.tudelft.aidm.optimalgroups.metric.bla.WorstRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public interface MatchingMetrics<FROM,TO>
{
	/**
	 * The gini coefficient of the preferences of assigning {@link FROM} to {@link TO}
	 */
	GiniCoefficient giniCoefficient();

	/**
	 * The average rank of assigned {@link TO}s in {@link FROM}s preferences
	 */
	AvgRank avgRank();

	/**
	 * The worst rank of assigned {@link TO}s in {@link FROM}s preferences
	 */
	WorstRank worstRank();

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
		public AvgRank avgRank()
		{
			return new AvgRankAssignedProjectToStudent(matching);
		}

		@Override
		public WorstRank worstRank()
		{
			return new WorstRankAssignedProjectToStudents(matching);
		}

		@Override
		public AUPCR aupcr()
		{
			return new AUPCRStudent(matching);
		}

		public ProjectProfileCurveStudents profileCurve()
		{
			return new ProjectProfileCurveStudents(matching);
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
		public AvgRank avgRank()
		{
			return new AvgRankAssignedProjectToGroup(matching);
		}

		@Override
		public WorstRank worstRank()
		{
			return new WorstRankAssignedProjectToGroup(matching);
		}

		@Override
		public AUPCR aupcr()
		{
			return new AUPCRGroup(matching);
		}
	}
}
