package nl.tudelft.aidm.optimalgroups.experiment;

import nl.tudelft.aidm.optimalgroups.algorithm.TopicGroupAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.matching.GiniCoefficientGroupRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.GiniCoefficientStudentRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.WorstAssignedProjectRankOfStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class ExperimentAlgorithmSubresult extends TopicGroupAlgorithm.Result
{
	public final GiniCoefficientStudentRank giniStudentRanking;
	public final GiniCoefficientGroupRank giniGroupAggregateRanking;

	public final AUPCRStudent aupcrStudent;
	public final AUPCRGroup aupcrGroup;

	public final ProjectProfileCurveStudents projectProfileCurveStudents;
	public final ProjectProfileCurveGroup projectProfileCurveGroup;

	public final WorstAssignedProjectRankOfStudents worstAssignedProjectRankOfStudents;

	public ExperimentAlgorithmSubresult(TopicGroupAlgorithm algo, Matching<Group.FormedGroup, Project> result)
	{
		super(algo, result);

		giniGroupAggregateRanking = new GiniCoefficientGroupRank(result);
		giniStudentRanking = new GiniCoefficientStudentRank(result);

		aupcrStudent = new AUPCRStudent(result);
		aupcrGroup = new AUPCRGroup(result);

		projectProfileCurveStudents = new ProjectProfileCurveStudents(result);
		projectProfileCurveGroup = new ProjectProfileCurveGroup(result);

		worstAssignedProjectRankOfStudents = new WorstAssignedProjectRankOfStudents(result);
	}
}
