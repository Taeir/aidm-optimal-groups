package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.SingleGroupPerProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysWithRandomGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMaxFlowMatchingORTOOLS;
import nl.tudelft.aidm.optimalgroups.algorithm.wip.EquallyLeastPopularProjects;
import nl.tudelft.aidm.optimalgroups.algorithm.wip.LeastPopularProject;
import nl.tudelft.aidm.optimalgroups.metric.AUPCR;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import org.sql2o.GenericDatasource;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Predicate;

public class StudentProjectORTOOLSMaxflowExperiment
{
	public static void main(String[] args)
	{
		DataSource dataSource;

		if (true)
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
		else
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/bepsys?serverTimezone=UTC", "root", "");

		int courseEdition = 10;

		// Fetch agents and from DB before loop; they don't change for another iteration
		Agents agents = Agents.from(dataSource, courseEdition);
		Projects projects = Projects.fromDb(dataSource, courseEdition);
		System.out.println("Amount of projects: " + projects.count());

		GroupSizeConstraint.fromDb groupSizeConstraint = new GroupSizeConstraint.fromDb(dataSource, courseEdition);
//		BepSysWithRandomGroups formedGroups = new BepSysWithRandomGroups(agents, groupSizeConstraint);
		//MaxFlow maxflow = new MaxFlow(formedGroups.finalFormedGroups(), projects);
//		RandomizedSerialDictatorship rsd = new RandomizedSerialDictatorship(formedGroups.finalFormedGroups(), projects);
//		= new StudentProjectMaxFlowMatchingORTOOLS(agents, projects, groupSizeConstraint.maxSize());

		//Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxflow.result();
//		Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxFlow.result();

//		Map<Project, List<Agent>> groupings = maxFlow.groupedByProject();

		Predicate<StudentProjectMaxFlowMatchingORTOOLS> terminationCondition = (StudentProjectMaxFlowMatchingORTOOLS m) ->
			m.groupedByProject().values().stream().allMatch(studentsAssignedToProject -> {
				// check if the following is true for each project
				var size = studentsAssignedToProject.size();
				return size >= groupSizeConstraint.minSize() || size == 0;
			}
		);

		StudentProjectMaxFlowMatchingORTOOLS resultingMatching  = solve(terminationCondition, projects, agents, groupSizeConstraint);

		var result = new HashMap<Project, java.util.Collection<Group.FormedGroup>>();

//		GroupSizeConstraint.fromDb groupSizeConstraint = new GroupSizeConstraint.fromDb(dataSource, 10);
		Map<Project, List<Agent>> groupedByProject = resultingMatching.groupedByProject();
		groupedByProject.forEach((proj, agentList) -> {
			Agents agentsWithProject = new Agents(agentList);
			BepSysWithRandomGroups bepSysWithRandomGroups = new BepSysWithRandomGroups(agentsWithProject, groupSizeConstraint, true);
			var groups = bepSysWithRandomGroups.asCollection();

			result.put(proj, groups);
		});

		System.out.print("Henk.....");


//		for (var match : matching.asList()) {
//			AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);
//
//			int rankNumber = assignedProjectRank.groupRank();
//			System.out.println("Group " + match.from().groupId() + " got project " + match.to().belongingToProject().id() + " (ranked as number " + rankNumber + ")");
//
//			assignedProjectRank.studentRanks().forEach(metric -> {
//				System.out.printf("\t\t-Student %s", metric.student().name);
//				System.out.printf(", rank: %s", metric.studentsRank());
//
//				System.out.printf("\t\t group satisfaction: %s\n", new GroupPreferenceSatisfaction(match, metric.student()).asFraction());
//			});
//		}
	}



	static StudentProjectMaxFlowMatchingORTOOLS solve(Predicate<StudentProjectMaxFlowMatchingORTOOLS> terminationCondition, Projects projects, Agents agents, GroupSizeConstraint groupSizeConstraint) {

		var matching = new StudentProjectMaxFlowMatchingORTOOLS(agents, projects, groupSizeConstraint.maxSize());
		var matchingGroupedByProject = matching.groupedByProject();
		var equallyLeastPopularProjects = new EquallyLeastPopularProjects(matchingGroupedByProject, groupSizeConstraint.maxSize());

		if (terminationCondition.test(matching)) {
			return matching;
		}

		var result = equallyLeastPopularProjects.asCollection ().stream()
			.map(leastPopularProject -> {
				Projects projectsWithoutLeastPopular = projects.without(leastPopularProject);

				var matchingWithProjectRemoved = solve(terminationCondition, projectsWithoutLeastPopular, agents, groupSizeConstraint);


				SingleGroupPerProjectMatching matchingWithSingleLargeGroupPerProject = new SingleGroupPerProjectMatching(matchingWithProjectRemoved);
				var metric = new AUPCR.StudentAUPCR(matchingWithSingleLargeGroupPerProject, projectsWithoutLeastPopular, agents);

				return new MatchingWithMetric(matching, metric);
			})
			.sorted(
					Comparator.comparing(
						(MatchingWithMetric matchingWithMetric) -> matchingWithMetric.metric.result()
					).reversed()
			)
			.findAny()
			.get(); // assume there's always one returned

		return result.matching;
	}

	static class Solution {
		final AUPCR metric;
		final StudentProjectMaxFlowMatchingORTOOLS solution;

		public Solution(AUPCR metric, StudentProjectMaxFlowMatchingORTOOLS solution)
		{
			this.metric = metric;
			this.solution = solution;
		}
	}

	static class MatchingWithMetric
	{
		public final StudentProjectMaxFlowMatchingORTOOLS matching;
		public final AUPCR metric;

		public MatchingWithMetric(StudentProjectMaxFlowMatchingORTOOLS matching, AUPCR metric)
		{
			this.matching = matching;
			this.metric = metric;
		}
	}

}
