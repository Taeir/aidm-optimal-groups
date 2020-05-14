package nl.tudelft.aidm.optimalgroups.experiment.variantvakken;

import nl.tudelft.aidm.optimalgroups.algorithm.AgentProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class ExperimentAlgorithmSubresult extends AgentProjectAlgorithm.Result
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

	public ExperimentAlgorithmSubresult(AgentProjectAlgorithm algo, AgentToProjectMatching result)
	{
		super(algo, result);

		studentPerspectiveMetrics = new MatchingMetrics.StudentProject(result);
	}
}
