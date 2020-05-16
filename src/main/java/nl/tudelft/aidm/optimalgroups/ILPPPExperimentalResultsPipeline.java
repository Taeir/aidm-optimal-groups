package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp.ILPPPDeterminedMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.AgentProjectMaxFlowMatching;
import nl.tudelft.aidm.optimalgroups.dataset.generated.GeneratedDataContext;
import nl.tudelft.aidm.optimalgroups.metric.*;
import nl.tudelft.aidm.optimalgroups.metric.matching.GroupPreferenceSatisfactionDistribution;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProfileCurveOfMatching;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.ProjectProfileCurveStudents;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedProjectRankGroupDistribution;
import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedProjectRankStudentDistribution;
import nl.tudelft.aidm.optimalgroups.model.*;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.time.Instant;

public class ILPPPExperimentalResultsPipeline
{
	public static String groupMatchingAlgorithm = "";
	public static String projectAssignmentAlgorithm = "";

	public static void main(String[] args) {

		groupMatchingAlgorithm = "BEPSysFixed";
		projectAssignmentAlgorithm = "ILPPP";

		for (int courseEditionId : values(/*3, 4,*/ 10))
		{
//			var courseEdition = CourseEdition.fromLocalBepSysDbSnapshot(courseEditionId);
			DatasetContext datasetContext = GeneratedDataContext.withNormallyDistributedProjectPreferences(150, 40, GroupSizeConstraint.manual(4,5), 4.0);

			AgentProjectMaxFlowMatching.flushCache(); // it's ok to reuse cache between aggregating methods - they don't impact maxflow! but dp flush between course editions just in case
			for (var preferenceAggregatingMethod : values(/*"Copeland",*/ "Borda"))
			{
				Application.preferenceAggregatingMethod = preferenceAggregatingMethod;

				System.out.printf("ILPPP %s CE %s, start: %d\n", preferenceAggregatingMethod, datasetContext.identifier(), Instant.now().getEpochSecond());
				henk(datasetContext, 1);
				System.out.printf("ILPPP %s CE %s, end: %d\n\n\n", preferenceAggregatingMethod, datasetContext.identifier(), Instant.now().getEpochSecond());
			}
		}
	}

	public static void henk(DatasetContext datasetContext, int iterations)
	{
		double[] studentAUPCRs = new double[iterations];
		double[] groupAUPCRs = new double[iterations];

		GroupPreferenceSatisfactionDistribution[] groupPreferenceSatisfactionDistributions = new GroupPreferenceSatisfactionDistribution[iterations];
		AssignedProjectRankGroupDistribution[] groupProjectRankDistributions = new AssignedProjectRankGroupDistribution[iterations];
		AssignedProjectRankStudentDistribution[] studentProjectRankDistributions = new AssignedProjectRankStudentDistribution[iterations];

		Agents agents = datasetContext.allAgents();
		Projects projects = datasetContext.allProjects();
		GroupSizeConstraint groupSizeConstraint = datasetContext.groupSizeConstraint();

		System.out.println("Amount of projects: " + projects.count());
		System.out.println("Amount of students: " + agents.count());

		// Perform the group making, project assignment and metric calculation inside the loop
		for (int iteration = 0; iteration < iterations; iteration++) {

//			GroupFormingAlgorithm formedGroups;
//			if (groupMatchingAlgorithm.equals("CombinedPreferencesGreedy")) {
//				formedGroups = new CombinedPreferencesGreedy(agents, groupSizeConstraint);
//			} else if (groupMatchingAlgorithm.equals("BEPSysFixed")) {
//				formedGroups = new BepSysImprovedGroups(agents, groupSizeConstraint, true);
//			} else {
//				formedGroups = new BepSysImprovedGroups(agents, groupSizeConstraint, false);
//			}
//
//			GroupProjectMatchings groupProjectMatching = null;
//
//			if (projectAssignmentAlgorithm.equals("RSD")) {
//				groupProjectMatching = new RandomizedSerialDictatorship(formedGroups.asFormedGroups(), projects);
//			} else {
//				groupProjectMatching = new GroupProjectMaxFlow(formedGroups.asFormedGroups(), projects);
//			}

			//Matchings<Group.FormedGroup, Project.ProjectSlot> matchings = maxflow.result();
			Matching<Group.FormedGroup, Project> matching = new ILPPPDeterminedMatching(datasetContext);
			var matchingFromStudentPerspective = AgentToProjectMatching.from(matching);

			ProfileCurveOfMatching studentProfileCurve = new ProjectProfileCurveStudents(matchingFromStudentPerspective);
			//studentProfile.printResult();

			ProfileCurveOfMatching groupProfileCurve = new ProjectProfileCurveGroup(matching);
			//groupProfile.printResult();

			AUPCR studentAUPCR = new AUPCRStudent(matchingFromStudentPerspective, projects, agents);
			//studentAUPCR.printResult();

			AUPCR groupAUPCR = new AUPCRGroup(matching, projects, agents);
			//groupAUPCR.printResult();

			GroupPreferenceSatisfactionDistribution groupPreferenceDistribution = new GroupPreferenceSatisfactionDistribution(matching, 20);
			//groupPreferenceDistribution.printResult();

			AssignedProjectRankGroupDistribution groupProjectRankDistribution = new AssignedProjectRankGroupDistribution(matching, projects);
			//groupProjectRankDistribution.printResult();

			AssignedProjectRankStudentDistribution studentProjectRankDistribution = new AssignedProjectRankStudentDistribution(matching, projects);
			//studentProjectRankDistribution.printResult();

			// Remember metrics
			studentAUPCRs[iteration] = studentAUPCR.asDouble();
			groupAUPCRs[iteration] = groupAUPCR.asDouble();
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
//		groupPreferenceSatisfactionDistribution.printResult();
		groupPreferenceSatisfactionDistribution.printToTxtFile(String.format("outputtxt/groupPreferenceSatisfaction_%s_Group%s_Preference%s_Project%s.txt", datasetContext.identifier(), groupMatchingAlgorithm, Application.preferenceAggregatingMethod, projectAssignmentAlgorithm));

		Distribution.AverageDistribution groupProjectRankDistribution = new Distribution.AverageDistribution(groupProjectRankDistributions);
//		groupProjectRankDistribution.printResult();
		groupProjectRankDistribution.printToTxtFile(String.format("outputtxt/groupProjectRank_%s_Group%s_Preference%s_Project%s.txt", datasetContext.identifier(), groupMatchingAlgorithm, Application.preferenceAggregatingMethod, projectAssignmentAlgorithm));

		Distribution.AverageDistribution studentProjectRankDistribution = new Distribution.AverageDistribution(studentProjectRankDistributions);
//		studentProjectRankDistribution.printResult();
		studentProjectRankDistribution.printToTxtFile(String.format( "outputtxt/studentProjectRank_%s_Group%s_Preference%s_Project%s.txt", datasetContext.identifier(), groupMatchingAlgorithm, Application.preferenceAggregatingMethod, projectAssignmentAlgorithm));

		System.out.printf("\n\tstudent AUPCR average over %d iterations: %f\n", iterations, studentAUPCRAverage);
		System.out.printf("\tgroup AUPCR average over %d iterations: %f\n", iterations, groupAUPCRAverage);
	}

	public static int[] values(int... args)
	{
		return args;
	}

	public static String[] values(String... args)
	{
		return args;
	}



//	public abstract static class ExperimentConfiguration
//	{
//		public final int iterations;
//		public final CourseEdition courseEditionId;
//		public final String groupMatchingAlgorithm;
//		public final String preferenceAggregatingMethod;
//		public final String projectAssignmentAlgorithm;
//
//		public ExperimentConfiguration(int iterations, String groupMatchingAlgorithm, String preferenceAggregatingMethod, String projectAssignmentAlgorithm)
//		{
//			this.iterations = iterations;
//			this.courseEditionId = courseEditionId;
//			this.groupMatchingAlgorithm = groupMatchingAlgorithm;
//			this.preferenceAggregatingMethod = preferenceAggregatingMethod;
//			this.projectAssignmentAlgorithm = projectAssignmentAlgorithm;
//		}
//
//		public abstract GroupFormingAlgorithm groupFormingAlgorithm();
//		public abstract GroupProjectMatchings groupProjectMatchings();
//	}
}
