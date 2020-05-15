package nl.tudelft.aidm.optimalgroups.experiment.agp;

import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

public class ExperimentAlgorithmSubresult extends GroupProjectAlgorithm.Result
{
//	public final GiniCoefficient giniStudentProjectRank;
//	public final GiniCoefficient giniGroupProjectRank;
//
//	public final AUPCR aupcrStudent;
//	public final AUPCR aupcrGroup;
//
//	public final ProjectProfileCurveStudents projectProfileCurveStudents;
//	public final ProjectProfileCurveGroup projectProfileCurveGroup;
//
//	public final WorstRankAssignedProjectToStudents worstRankAssignedProjectToStudents;

	public final MatchingMetrics.StudentProject studentPerspectiveMetrics;
	public final MatchingMetrics.GroupProject groupPerspectiveMetrics;

	public ExperimentAlgorithmSubresult(GroupProjectAlgorithm algo, GroupToProjectMatching<Group.FormedGroup> result)
	{
		super(algo, result);

		var studentPerspective = AgentToProjectMatching.from(result);
		studentPerspectiveMetrics = new MatchingMetrics.StudentProject(studentPerspective);
		groupPerspectiveMetrics = new MatchingMetrics.GroupProject(result);
	}
}
