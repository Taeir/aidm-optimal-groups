package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.*;
import nl.tudelft.aidm.optimalgroups.algorithm.project.*;
import nl.tudelft.aidm.optimalgroups.metric.*;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import org.sql2o.GenericDatasource;

import javax.sql.DataSource;
import java.util.Map;

public class Application
{
	public static final int iterations = 10;

	public static void main(String[] args) {
		DataSource dataSource;

		if (false)
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
		else
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/bepsys?serverTimezone=UTC", "root", "root");

		float[] studentAUPCRs = new float[iterations];
		float[] groupAUPCRs = new float[iterations];

		GroupPreferenceSatisfactionDistribution[] groupPreferenceSatisfactionDistributions = new GroupPreferenceSatisfactionDistribution[iterations];
		AssignedProjectRankDistribution[] groupProjectRankDistributions = new AssignedProjectRankDistribution[iterations];
		AssignedProjectRankStudentDistribution[] studentProjectRankDistributions = new AssignedProjectRankStudentDistribution[iterations];

		// Fetch agents and from DB before loop; they don't change for another iteration
		Agents agents = Agents.from(dataSource, 4);
		Projects projects = Projects.fromDb(dataSource, 4);
		System.out.println("Amount of projects: " + projects.count());

		// Perform the group making, project assignment and metric calculation inside the loop
		for (int iteration = 0; iteration < iterations; iteration++) {

			BepSysWithRandomGroups formedGroups = new BepSysWithRandomGroups(agents, 4, 6);
			//MaxFlow maxflow = new MaxFlow(formedGroups.finalFormedGroups(), projects);
			RandomizedSerialDictatorship rsd = new RandomizedSerialDictatorship(formedGroups.finalFormedGroups(), projects);

			//Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxflow.result();
			Matching<Group.FormedGroup, Project.ProjectSlot> matching = rsd.result();

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

			AssignedProjectRankDistribution groupProjectRankDistribution = new AssignedProjectRankDistribution(matching, projects.count());
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

		Distribution.AverageDistribution groupProjectRankDistribution = new Distribution.AverageDistribution(groupProjectRankDistributions);
		groupProjectRankDistribution.printResult();

		Distribution.AverageDistribution studentProjectRankDistribution = new Distribution.AverageDistribution(studentProjectRankDistributions);
		studentProjectRankDistribution.printResult();

		System.out.printf("\n\nstudent AUPCR average over %d iterations: %f\n", iterations, studentAUPCRAverage);
		System.out.printf("group AUPCR average over %d iterations: %f\n", iterations, groupAUPCRAverage);
	}
}
