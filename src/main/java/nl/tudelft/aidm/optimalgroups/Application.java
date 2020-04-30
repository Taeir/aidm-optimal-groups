package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.*;
import nl.tudelft.aidm.optimalgroups.algorithm.project.*;
import nl.tudelft.aidm.optimalgroups.dataset.generated.GeneratedDataContext;
import nl.tudelft.aidm.optimalgroups.metric.*;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Application
{
	public static final int courseEditionId = 10;
	public static final int iterations = 10;
	public static final String groupMatchingAlgorithm = "CombinedPreferencesGreedy";
	public static String preferenceAggregatingMethod = "Copeland";
	public static final String projectAssignmentAlgorithm = "MaxFlow";

	public static void main(String[] args)
	{
		// "Fetch" agents and from DB before loop; they don't change for another iteration
//		DatasetContext datasetContext = CourseEdition.fromLocalBepSysDbSnapshot(courseEditionId);
		DatasetContext datasetContext = new GeneratedDataContext(150, 40, GroupSizeConstraint.basic(4,5));
		printDatasetInfo(datasetContext);

		float[] studentAUPCRs = new float[iterations];
		float[] groupAUPCRs = new float[iterations];

		GroupPreferenceSatisfactionDistribution[] groupPreferenceSatisfactionDistributions = new GroupPreferenceSatisfactionDistribution[iterations];
		AssignedProjectRankGroupDistribution[] groupProjectRankDistributions = new AssignedProjectRankGroupDistribution[iterations];
		AssignedProjectRankStudentDistribution[] studentProjectRankDistributions = new AssignedProjectRankStudentDistribution[iterations];

		// Perform the group making, project assignment and metric calculation inside the loop
		for (int iteration = 0; iteration < iterations; iteration++) {

			printIterationNumber(iteration);

			GroupFormingAlgorithm formedGroups = formGroups(datasetContext);
			GroupProjectMatching<Group.FormedGroup> groupProjectMatching = assignGroupsToProjects(datasetContext, formedGroups);

			//Matchings<Group.FormedGroup, Project.ProjectSlot> matchings = maxflow.result();
			Matching<Group.FormedGroup, Project> matching = groupProjectMatching;

			Profile studentProfile = new Profile.StudentProjectProfile(matching);
			//studentProfile.printResult();

			Profile groupProfile = new Profile.GroupProjectProfile(matching);
			//groupProfile.printResult();

			AUPCR studentAUPCR = new AUPCR.StudentAUPCR(matching, datasetContext.allProjects(), datasetContext.allAgents());
			//studentAUPCR.printResult();

			AUPCR groupAUPCR = new AUPCR.GroupAUPCR(matching, datasetContext.allProjects(), datasetContext.allAgents());
			//groupAUPCR.printResult();

			GroupPreferenceSatisfactionDistribution groupPreferenceDistribution = new GroupPreferenceSatisfactionDistribution(matching, 20);
			//groupPreferenceDistribution.printResult();

			AssignedProjectRankGroupDistribution groupProjectRankDistribution = new AssignedProjectRankGroupDistribution(matching, datasetContext.allProjects());
			//groupProjectRankDistribution.printResult();

			AssignedProjectRankStudentDistribution studentProjectRankDistribution = new AssignedProjectRankStudentDistribution(matching, datasetContext.allProjects());
			//studentProjectRankDistribution.printResult();

			// Remember metrics
			studentAUPCRs[iteration] = studentAUPCR.result();
			groupAUPCRs[iteration] = groupAUPCR.result();
			groupPreferenceSatisfactionDistributions[iteration] = groupPreferenceDistribution;
			groupProjectRankDistributions[iteration] = groupProjectRankDistribution;
			studentProjectRankDistributions[iteration] = studentProjectRankDistribution;
		}

		// Calculate all the averages and print them to the console
		float studentAUPCRAverage = 0;
		float groupAUPCRAverage = 0;
		for (int iteration = 0; iteration < iterations; iteration++) {
			studentAUPCRAverage += studentAUPCRs[iteration] / studentAUPCRs.length;
			groupAUPCRAverage += groupAUPCRs[iteration] / groupAUPCRs.length;
		}

		Distribution.AverageDistribution groupPreferenceSatisfactionDistribution = new Distribution.AverageDistribution(groupPreferenceSatisfactionDistributions);
		groupPreferenceSatisfactionDistribution.printToTxtFile("outputtxt/groupPreferenceSatisfaction.txt");
		//groupPreferenceSatisfactionDistribution.printResult();

		printAveragePeerSatisfaction(groupPreferenceSatisfactionDistribution);

		Distribution.AverageDistribution groupProjectRankDistribution = new Distribution.AverageDistribution(groupProjectRankDistributions);
		groupProjectRankDistribution.printToTxtFile("outputtxt/groupProjectRank.txt");
		//groupProjectRankDistribution.printResult();

		Distribution.AverageDistribution studentProjectRankDistribution = new Distribution.AverageDistribution(studentProjectRankDistributions);
		studentProjectRankDistribution.printToTxtFile("outputtxt/studentProjectRank.txt");
		//studentProjectRankDistribution.printResult();

		printStudentAupcrAverage(studentAUPCRAverage);
		writeToFile("outputtxt/studentAUPCR.txt", String.valueOf(studentAUPCRAverage));

		printGroupAupcrAverage(groupAUPCRAverage);
		writeToFile("outputtxt/groupAUPCR.txt", String.valueOf(groupAUPCRAverage));
	}

	private static void printDatasetInfo(DatasetContext courseEdition)
	{
		System.out.println("Amount of projects: " + courseEdition.allProjects().count());
		System.out.println("Amount of students: " + courseEdition.allAgents().count());
	}

	private static GroupProjectMatching<Group.FormedGroup> assignGroupsToProjects(DatasetContext datasetContext, GroupFormingAlgorithm formedGroups)
	{
		if (projectAssignmentAlgorithm.equals("RSD")) {
			return new RandomizedSerialDictatorship(formedGroups.asFormedGroups(), datasetContext.allProjects());
		} else {
			return new GroupProjectMaxFlow(formedGroups.asFormedGroups(), datasetContext.allProjects());
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

	private static void printIterationNumber(int iteration)
	{
		System.out.printf("Iteration %d\n", iteration+1);
	}

	private static void printAveragePeerSatisfaction(Distribution.AverageDistribution groupPreferenceSatisfactionDistribution)
	{
		System.out.println("Average group preference satisfaction: " + groupPreferenceSatisfactionDistribution.average());
	}

	private static void printGroupAupcrAverage(float groupAUPCRAverage)
	{
		System.out.printf("group AUPCR average over %d iterations: %f\n", iterations, groupAUPCRAverage);
	}

	private static void printStudentAupcrAverage(float studentAUPCRAverage)
	{
		System.out.printf("\n\nstudent AUPCR average over %d iterations: %f\n", iterations, studentAUPCRAverage);
	}
}
