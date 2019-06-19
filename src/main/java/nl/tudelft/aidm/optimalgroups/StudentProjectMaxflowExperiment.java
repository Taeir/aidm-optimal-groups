package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysWithRandomGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMaxFlow;
import nl.tudelft.aidm.optimalgroups.algorithm.wip.LeastPopularProject;
import nl.tudelft.aidm.optimalgroups.metric.AssignedProjectRank;
import nl.tudelft.aidm.optimalgroups.metric.GroupPreferenceSatisfaction;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import org.sql2o.GenericDatasource;

import javax.sql.DataSource;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentProjectMaxflowExperiment
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
		StudentProjectMaxFlow maxFlow = StudentProjectMaxFlow.of(agents, projects);

		//Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxflow.result();
		Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxFlow.result();

		Map<Project, List<Agent>> groupings = maxFlow.groupedByProject();

		Predicate<Map<Project, List<Agent>>> terminationCondition = (Map<Project, List<Agent>> g) -> g.values().stream().allMatch(assignedToProject -> {
			var size = assignedToProject.size();
			return size >= 4 || size == 0;
		});

		while (terminationCondition.test(groupings) == false) {
			LeastPopularProject leastPopularProject = new LeastPopularProject(groupings);

			Projects projectsWithoutLeastPopular = projects.without(leastPopularProject);
			maxFlow = StudentProjectMaxFlow.of(agents, projectsWithoutLeastPopular);
			groupings = maxFlow.groupedByProject();

			projects = Projects.from(new ArrayList<>(groupings.keySet()));
		}

		System.out.print("Henk.....");


//		for (var match : matching.asList()) {
//			AssignedProjectRank assignedProjectRank = new AssignedProjectRank(match);
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
}
