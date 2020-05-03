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
import nl.tudelft.aidm.optimalgroups.metric.matching.GroupPreferenceSatisfactionDistribution;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankGroupDistribution;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudentDistribution;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import static nl.tudelft.aidm.optimalgroups.Application.groupMatchingAlgorithm;
import static nl.tudelft.aidm.optimalgroups.Application.projectAssignmentAlgorithm;

public class Experiment
{
	private DatasetContext datasetContext;
	private List<TopicGroupAlgorithm> matchingAlgorithm;

	// unsupported for now
	private final int numIterations = 1;

	static class ExperimentResult
	{
		private final Experiment experiment;

		List<ExperimentAlgorithmResult> results;
		PopularityMatrix popularityMatrix;
		AvgPreferenceRankOfProjects projectRankingDistribution;

		public ExperimentResult(Experiment experiment, List<ExperimentAlgorithmResult> results)
		{
			this.experiment = experiment;
			this.results = results;

			this.popularityMatrix = new PopularityMatrixFromExperimentResults(results);
			projectRankingDistribution = AvgPreferenceRankOfProjects.ofAgentsInDatasetContext(experiment.datasetContext);
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

	static class ExperimentAlgorithmResult extends TopicGroupAlgorithm.Result
	{
		GiniCoefficientStudentRank giniStudentRanking;
		GiniCoefficientGroupRank giniGroupAggregateRanking;

		AUPCRStudent aupcrStudent;
		AUPCRGroup aupcrGroup;

		public ExperimentAlgorithmResult(TopicGroupAlgorithm algo, Matching<Group.FormedGroup, Project> result)
		{
			super(algo, result);

			giniGroupAggregateRanking = new GiniCoefficientGroupRank(result);
			giniStudentRanking = new GiniCoefficientStudentRank(result);

			aupcrStudent = new AUPCRStudent(result);
			aupcrGroup = new AUPCRGroup(result);
		}
	}

	public void run()
	{
		printDatasetInfo(System.out);

		var avgPreferenceRankOfProjects = AvgPreferenceRankOfProjects.fromAgents(datasetContext.allAgents(), datasetContext.allProjects());
		avgPreferenceRankOfProjects.displayChart();

		float[] studentAUPCRs = new float[numIterations];
		float[] groupAUPCRs = new float[numIterations];

		GroupPreferenceSatisfactionDistribution[] groupPreferenceSatisfactionDistributions = new GroupPreferenceSatisfactionDistribution[numIterations];
		AssignedProjectRankGroupDistribution[] groupProjectRankDistributions = new AssignedProjectRankGroupDistribution[numIterations];
		AssignedProjectRankStudentDistribution[] studentProjectRankDistributions = new AssignedProjectRankStudentDistribution[numIterations];

		for (TopicGroupAlgorithm algorithm : matchingAlgorithm)
		{
			// Perform the group making, project assignment and metric calculation inside the loop
			for (int iteration = 0; iteration < numIterations; iteration++)
			{
				runIteration(iteration, algorithm);
			}

			// Calculate all the averages and print them to the console
			float studentAUPCRAverage = 0;
			float groupAUPCRAverage = 0;
			for (int iteration = 0; iteration < numIterations; iteration++) {
				studentAUPCRAverage += studentAUPCRs[iteration] / studentAUPCRs.length;
				groupAUPCRAverage += groupAUPCRs[iteration] / groupAUPCRs.length;
			}

//			Distribution.AverageDistribution groupPreferenceSatisfactionDistribution = new Distribution.AverageDistribution(groupPreferenceSatisfactionDistributions);
//			groupPreferenceSatisfactionDistribution.printToTxtFile("outputtxt/groupPreferenceSatisfaction.txt");
//			//groupPreferenceSatisfactionDistribution.printResult();
//
//			printAveragePeerSatisfaction(groupPreferenceSatisfactionDistribution);
//
//			Distribution.AverageDistribution groupProjectRankDistribution = new Distribution.AverageDistribution(groupProjectRankDistributions);
//			groupProjectRankDistribution.printToTxtFile("outputtxt/groupProjectRank.txt");
//			//groupProjectRankDistribution.printResult();
//
//			Distribution.AverageDistribution studentProjectRankDistribution = new Distribution.AverageDistribution(studentProjectRankDistributions);
//			studentProjectRankDistribution.printToTxtFile("outputtxt/studentProjectRank.txt");
//			//studentProjectRankDistribution.printResult();

			printStudentAupcrAverage(studentAUPCRAverage);
			writeToFile("outputtxt/studentAUPCR.txt", String.valueOf(studentAUPCRAverage));

			printGroupAupcrAverage(groupAUPCRAverage);
			writeToFile("outputtxt/groupAUPCR.txt", String.valueOf(groupAUPCRAverage));
		}


	}

	public void runIteration(int iteration, TopicGroupAlgorithm algorithm)
	{
			printIterationNumber(iteration);

			GroupFormingAlgorithm formedGroups = formGroups(datasetContext);
			GroupProjectMatching<Group.FormedGroup> groupProjectMatching =  assignGroupsToProjects(datasetContext, formedGroups);

//			Matching<Group.FormedGroup, Project> matching = new ILPPPDeterminedMatching(datasetContext);

			Matching<Group.FormedGroup, Project> matching = algorithm.determineMatching(datasetContext);

			var studentProfileCurve = new ProjectProfileCurveStudents(matching);
			studentProfileCurve.displayChart();

//			ProfileCurveOfMatching groupProfileCurve = new ProjectProfileCurveGroup(matching);
//			groupProfile.printResult();

			GiniCoefficientStudentRank giniStudentRank = new GiniCoefficientStudentRank(matching);
			giniStudentRank.printResult(System.out);

			AUPCR studentAUPCR = new AUPCRStudent(matching, datasetContext.allProjects(), datasetContext.allAgents());
			//studentAUPCR.printResult();

			AUPCR groupAUPCR = new AUPCRGroup(matching, datasetContext.allProjects(), datasetContext.allAgents());
			//groupAUPCR.printResult();

			GroupPreferenceSatisfactionDistribution groupPreferenceDistribution = new GroupPreferenceSatisfactionDistribution(matching, 20);
			//groupPreferenceDistribution.printResult();

			AssignedProjectRankGroupDistribution groupProjectRankDistribution = new AssignedProjectRankGroupDistribution(matching, datasetContext.allProjects());
			//groupProjectRankDistribution.printResult();

			AssignedProjectRankStudentDistribution studentProjectRankDistribution = new AssignedProjectRankStudentDistribution(matching, datasetContext.allProjects());
			//studentProjectRankDistribution.printResult();

			// Remember metrics
//			studentAUPCRs[iteration] = studentAUPCR.result();
//			groupAUPCRs[iteration] = groupAUPCR.result();
//			groupPreferenceSatisfactionDistributions[iteration] = groupPreferenceDistribution;
//			groupProjectRankDistributions[iteration] = groupProjectRankDistribution;
//			studentProjectRankDistributions[iteration] = studentProjectRankDistribution;
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
