package nl.tudelft.aidm.optimalgroups.experiment;

import nl.tudelft.aidm.optimalgroups.algorithm.TopicGroupAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.group.CombinedPreferencesGreedy;
import nl.tudelft.aidm.optimalgroups.algorithm.group.GroupFormingAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMaxFlow;
import nl.tudelft.aidm.optimalgroups.algorithm.project.RandomizedSerialDictatorship;
import nl.tudelft.aidm.optimalgroups.metric.Distribution;
import nl.tudelft.aidm.optimalgroups.metric.PopularityMatrix;
import nl.tudelft.aidm.optimalgroups.metric.dataset.AvgPreferenceRankOfProjects;
import nl.tudelft.aidm.optimalgroups.metric.matching.GiniCoefficientGroupRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.GiniCoefficientStudentRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.WorstAssignedProjectRankOfStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.tudelft.aidm.optimalgroups.Application.*;

public class Experiment
{
	private DatasetContext datasetContext;
	private List<TopicGroupAlgorithm> matchingAlgorithm;
	public final AvgPreferenceRankOfProjects projectRankingDistribution;

	// unsupported for now
	private final int numIterations = 1;

	public Experiment(DatasetContext datasetContext, List<TopicGroupAlgorithm> matchingAlgorithm)
	{
		this.datasetContext = datasetContext;
		this.matchingAlgorithm = matchingAlgorithm;
		projectRankingDistribution = AvgPreferenceRankOfProjects.ofAgentsInDatasetContext(datasetContext);
	}

	public static class ExperimentResult
	{
		private final Experiment experiment;

		public final List<ExperimentAlgorithmResult> results;
		public final PopularityMatrix popularityMatrix;

		public ExperimentResult(Experiment experiment, List<ExperimentAlgorithmResult> results)
		{
			this.experiment = experiment;
			this.results = Collections.unmodifiableList(results);

			this.popularityMatrix = new PopularityMatrixFromExperimentResults(results);
		}
	}

//	static class ExperimentAlgorithmResult
//	{
//
//		DatasetContext datasetContext;
//		AvgPreferenceRankOfProjects projectRankingDistribution;
//
//		List<ExperimentAlgorithmIterationResult> iterationResults;
//
//	}

	public static class ExperimentAlgorithmResult extends TopicGroupAlgorithm.Result
	{
		public final GiniCoefficientStudentRank giniStudentRanking;
		public final GiniCoefficientGroupRank giniGroupAggregateRanking;

		public final AUPCRStudent aupcrStudent;
		public final AUPCRGroup aupcrGroup;

		public final ProjectProfileCurveStudents projectProfileCurveStudents;
		public final ProjectProfileCurveGroup projectProfileCurveGroup;

		public final WorstAssignedProjectRankOfStudents worstAssignedProjectRankOfStudents;

		public ExperimentAlgorithmResult(TopicGroupAlgorithm algo, Matching<Group.FormedGroup, Project> result)
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

	public ExperimentResult result()
	{

		var results = new ArrayList<ExperimentAlgorithmResult>();

		for (TopicGroupAlgorithm algorithm : matchingAlgorithm)
		{
			var resultingMatching = algorithm.determineMatching(datasetContext);
			results.add(new ExperimentAlgorithmResult(algorithm, resultingMatching));
		}

		return new ExperimentResult(this, results);
	}

	private void printDatasetInfo(PrintStream printStream)
	{
		printStream.println("Amount of projects: " + datasetContext.allProjects().count());
		printStream.println("Amount of students: " + datasetContext.allAgents().count());
	}

	private static GroupProjectMatching<Group.FormedGroup> assignGroupsToProjects(DatasetContext datasetContext, GroupFormingAlgorithm formedGroups)
	{
		if (projectAssignmentAlgorithm.equals("RSD")) {
			return new RandomizedSerialDictatorship(datasetContext, formedGroups.asFormedGroups(), datasetContext.allProjects());
		} else {
			return new GroupProjectMaxFlow(datasetContext, formedGroups.asFormedGroups(), datasetContext.allProjects());
		}
	}

	private static GroupFormingAlgorithm formGroups(DatasetContext courseEdition)
	{
		if (groupMatchingAlgorithm.equals("CombinedPreferencesGreedy")) {
			return new CombinedPreferencesGreedy(courseEdition);
		}
		else if (groupMatchingAlgorithm.equals("BEPSysFixed")) {
			return new BepSysImprovedGroups(courseEdition.allAgents(), courseEdition.groupSizeConstraint(), true);
		}
		else {
			return new BepSysImprovedGroups(courseEdition.allAgents(), courseEdition.groupSizeConstraint(), false);
		}
	}

	public static void writeToFile(String fileName, String content) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
			writer.write(content);
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void printIterationNumber(int iteration)
	{
		System.out.printf("Iteration %d\n", iteration+1);
	}

	private void printAveragePeerSatisfaction(Distribution.AverageDistribution groupPreferenceSatisfactionDistribution)
	{
		System.out.println("Average group preference satisfaction: " + groupPreferenceSatisfactionDistribution.average());
	}

	private void printGroupAupcrAverage(float groupAUPCRAverage)
	{
		System.out.printf("AUPCR - Group aggregate pref (average over %d iterations: %f)\n", numIterations, groupAUPCRAverage);
	}

	private void printStudentAupcrAverage(float studentAUPCRAverage)
	{
		System.out.printf("AUPCR - Individual student   (average over %d iterations: %f)\n", numIterations, studentAUPCRAverage);
	}
}
