package nl.tudelft.aidm.optimalgroups.experiment.thesis;

import nl.tudelft.aidm.optimalgroups.algorithm.TopicGroupAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.bla.GiniCoefficient;
import nl.tudelft.aidm.optimalgroups.metric.matching.GiniCoefficientGroupRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.GiniCoefficientStudentRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.matching.WorstRankAssignedProjectToStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class ExperimentAlgorithmSubresult extends TopicGroupAlgorithm.Result
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

	public ExperimentAlgorithmSubresult(TopicGroupAlgorithm algo, GroupToProjectMatching<Group.FormedGroup> result)
	{
		super(algo, result);

		var studentPerspective = AgentToProjectMatching.from(result);
		studentPerspectiveMetrics = new MatchingMetrics.StudentProject(studentPerspective);
		groupPerspectiveMetrics = new MatchingMetrics.GroupProject(result);
	}
}
