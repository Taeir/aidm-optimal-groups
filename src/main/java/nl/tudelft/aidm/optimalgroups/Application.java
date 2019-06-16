package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.*;
import nl.tudelft.aidm.optimalgroups.algorithm.project.*;
import nl.tudelft.aidm.optimalgroups.metric.*;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import org.sql2o.GenericDatasource;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.List;

public class Application
{
	public static final int iterations = 200;
	public static final int courseEdition = 4;
	public static final String projectAssignmentAlgorithm = "RSD";

	public static void main(String[] args) {
		DataSource dataSource;

		if (true)
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/test", "henk", "henk");
		else
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/bepsys?serverTimezone=UTC", "root", "");

		int[] groupSizes = getGroupSizes(dataSource);
		int minGroupSize = groupSizes[0];
		int maxGroupSize = groupSizes[1];



		float[] studentAUPCRs = new float[iterations];
		float[] groupAUPCRs = new float[iterations];

		GroupPreferenceSatisfactionDistribution[] groupPreferenceSatisfactionDistributions = new GroupPreferenceSatisfactionDistribution[iterations];
		AssignedProjectRankDistribution[] groupProjectRankDistributions = new AssignedProjectRankDistribution[iterations];
		AssignedProjectRankStudentDistribution[] studentProjectRankDistributions = new AssignedProjectRankStudentDistribution[iterations];

		// Fetch agents and from DB before loop; they don't change for another iteration
		Agents agents = Agents.from(dataSource, courseEdition);
		Projects projects = Projects.fromDb(dataSource, courseEdition);
		System.out.println("Amount of projects: " + projects.count());

		// Perform the group making, project assignment and metric calculation inside the loop
		for (int iteration = 0; iteration < iterations; iteration++) {

			BepSysWithRandomGroups formedGroups = new BepSysWithRandomGroups(agents, minGroupSize, maxGroupSize);

			ProjectMatchingAlgorithm projectMatchingAlgorithm = null;
			if (projectAssignmentAlgorithm == "RSD") {
				projectMatchingAlgorithm = new RandomizedSerialDictatorship(formedGroups.finalFormedGroups(), projects);
			} else {
				projectMatchingAlgorithm = new GroupProjectMaxFlow(formedGroups.finalFormedGroups(), projects);
			}

			//Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxflow.result();
			Matching<Group.FormedGroup, Project.ProjectSlot> matching = projectMatchingAlgorithm.result();

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
		groupPreferenceSatisfactionDistribution.printToTxtFile(String.format("outputtxt/groupPreferenceSatisfaction_CE%d_Project%s.txt", courseEdition, projectAssignmentAlgorithm));

		Distribution.AverageDistribution groupProjectRankDistribution = new Distribution.AverageDistribution(groupProjectRankDistributions);
		groupProjectRankDistribution.printResult();
		groupProjectRankDistribution.printToTxtFile(String.format("outputtxt/groupProjectRank_CE%d_Project%s.txt", courseEdition, projectAssignmentAlgorithm));

		Distribution.AverageDistribution studentProjectRankDistribution = new Distribution.AverageDistribution(studentProjectRankDistributions);
		studentProjectRankDistribution.printResult();
		studentProjectRankDistribution.printToTxtFile(String.format( "outputtxt/studentProjectRank_CE%d_Project%s.txt", courseEdition, projectAssignmentAlgorithm));

		System.out.printf("\n\nstudent AUPCR average over %d iterations: %f\n", iterations, studentAUPCRAverage);
		System.out.printf("group AUPCR average over %d iterations: %f\n", iterations, groupAUPCRAverage);
	}


	public static int[] getGroupSizes(DataSource dataSource)
	{
		int[] groupSizes = new int[2];
		var sql = "SELECT min_group_size FROM course_configurations where course_edition_id = " + courseEdition;
		var sql2 = "SELECT max_group_size FROM course_configurations where course_edition_id = " + courseEdition;
		try (var connection = new Sql2o(dataSource).open())
		{
			Query query = connection.createQuery(sql);
			List<Integer> minGroupSizes = query.executeAndFetch(
					(ResultSetHandler<Integer>) rs ->
							(rs.getInt("min_group_size"))
			);
			Query query2 = connection.createQuery(sql2);
			List<Integer> maxGroupSizes = query2.executeAndFetch(
					(ResultSetHandler<Integer>) rs ->
							(rs.getInt("max_group_size"))
			);
			groupSizes[0] = minGroupSizes.get(0);
			groupSizes[1] = maxGroupSizes.get(0);
		}
		return groupSizes;
	}
}
