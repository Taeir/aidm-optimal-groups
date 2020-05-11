package nl.tudelft.aidm.optimalgroups.experiment.variantvakken;

import nl.tudelft.aidm.optimalgroups.algorithm.StudentProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.TopicGroupAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

public class ExperimentAlgorithmSubresult extends StudentProjectAlgorithm.Result
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

	public ExperimentAlgorithmSubresult(StudentProjectAlgorithm algo, AgentToProjectMatching result)
	{
		super(algo, result);

		studentPerspectiveMetrics = new MatchingMetrics.StudentProject(result);
	}
}
