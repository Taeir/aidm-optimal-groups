package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.*;
import nl.tudelft.aidm.optimalgroups.algorithm.project.*;
import nl.tudelft.aidm.optimalgroups.metric.*;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import org.sql2o.GenericDatasource;

import javax.sql.DataSource;

public class Application
{
	public static final int iterations = 1;
	public static final int courseEdition = 4;
	public static final String groupMatchingAlgorithm = "CombinedPreferencesGreedy";
	public static final String preferenceAggregatingMethod = "Copeland";
	public static final String projectAssignmentAlgorithm = "RSD";

	public static void main(String[] args) {
		DataSource dataSource;

		if (true)
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/test", "henk", "henk");
		else
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/bepsys?serverTimezone=UTC", "root", "");

		GroupSizeConstraint.fromDb groupSizeConstraint = new GroupSizeConstraint.fromDb(dataSource, courseEdition);

		float[] studentAUPCRs = new float[iterations];
		float[] groupAUPCRs = new float[iterations];

		GroupPreferenceSatisfactionDistribution[] groupPreferenceSatisfactionDistributions = new GroupPreferenceSatisfactionDistribution[iterations];
		AssignedProjectRankGroupDistribution[] groupProjectRankDistributions = new AssignedProjectRankGroupDistribution[iterations];
		AssignedProjectRankStudentDistribution[] studentProjectRankDistributions = new AssignedProjectRankStudentDistribution[iterations];

		// Fetch agents and from DB before loop; they don't change for another iteration
		Agents agents = Agents.from(dataSource, courseEdition);
		Projects projects = Projects.fromDb(dataSource, courseEdition);
		System.out.println("Amount of projects: " + projects.count());
		System.out.println("Amount of students: " + agents.count());

		// Perform the group making, project assignment and metric calculation inside the loop
		for (int iteration = 0; iteration < iterations; iteration++) {

			GroupFormingAlgorithm formedGroups;
			if (groupMatchingAlgorithm.equals("CombinedPreferencesGreedy")) {
				formedGroups = new CombinedPreferencesGreedy(agents, groupSizeConstraint);
			} else if (groupMatchingAlgorithm.equals("BEPSysFixed")) {
				formedGroups = new BepSysWithRandomGroups(agents, groupSizeConstraint, true);
			} else {
				formedGroups = new BepSysWithRandomGroups(agents, groupSizeConstraint, false);
			}

			GroupProjectMatching groupProjectMatching = null;

			if (projectAssignmentAlgorithm.equals("RSD")) {
				groupProjectMatching = new RandomizedSerialDictatorship(formedGroups.finalFormedGroups(), projects);
			} else {
				groupProjectMatching = new GroupProjectMaxFlow(formedGroups.finalFormedGroups(), projects);
			}

			//Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxflow.result();
			Matching<Group.FormedGroup, Project> matching = groupProjectMatching;

			Profile studentProfile = new Profile.StudentProjectProfile(matching);
			//studentProfile.printResult();

			Profile groupProfile = new Profile.GroupProjectProfile(matching);
			//groupProfile.printResult();

			AUPCR studentAUPCR = new AUPCR.StudentAUPCR(matching, projects, agents);
			//studentAUPCR.printResult();

			AUPCR groupAUPCR = new AUPCR.GroupAUPCR(matching, projects, agents);
			//groupAUPCR.printResult();

			GroupPreferenceSatisfactionDistribution groupPreferenceDistribution = new GroupPreferenceSatisfactionDistribution(matching, 20);
			//groupPreferenceDistribution.printResult();

			AssignedProjectRankGroupDistribution groupProjectRankDistribution = new AssignedProjectRankGroupDistribution(matching, projects.count());
			//groupProjectRankDistribution.printResult();

			AssignedProjectRankStudentDistribution studentProjectRankDistribution = new AssignedProjectRankStudentDistribution(matching, projects.count());
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
			studentAUPCRAverage += studentAUPCRs[iteration];
			groupAUPCRAverage += groupAUPCRs[iteration];
		}

		studentAUPCRAverage = studentAUPCRAverage / studentAUPCRs.length;
		groupAUPCRAverage = groupAUPCRAverage / groupAUPCRs.length;

		Distribution.AverageDistribution groupPreferenceSatisfactionDistribution = new Distribution.AverageDistribution(groupPreferenceSatisfactionDistributions);
		groupPreferenceSatisfactionDistribution.printResult();
		groupPreferenceSatisfactionDistribution.printToTxtFile(String.format("outputtxt/groupPreferenceSatisfaction_CE%d_Group%s_Preference%s_Project%s.txt", courseEdition, groupMatchingAlgorithm, preferenceAggregatingMethod, projectAssignmentAlgorithm));

		Distribution.AverageDistribution groupProjectRankDistribution = new Distribution.AverageDistribution(groupProjectRankDistributions);
		groupProjectRankDistribution.printResult();
		groupProjectRankDistribution.printToTxtFile(String.format("outputtxt/groupProjectRank_CE%d_Group%s_Preference%s_Project%s.txt", courseEdition, groupMatchingAlgorithm, preferenceAggregatingMethod, projectAssignmentAlgorithm));

		Distribution.AverageDistribution studentProjectRankDistribution = new Distribution.AverageDistribution(studentProjectRankDistributions);
		studentProjectRankDistribution.printResult();
		studentProjectRankDistribution.printToTxtFile(String.format( "outputtxt/studentProjectRank_CE%d_Group%s_Preference%s_Project%s.txt", courseEdition, groupMatchingAlgorithm, preferenceAggregatingMethod, projectAssignmentAlgorithm));

		System.out.printf("\n\nstudent AUPCR average over %d iterations: %f\n", iterations, studentAUPCRAverage);
		System.out.printf("group AUPCR average over %d iterations: %f\n", iterations, groupAUPCRAverage);
	}
}
