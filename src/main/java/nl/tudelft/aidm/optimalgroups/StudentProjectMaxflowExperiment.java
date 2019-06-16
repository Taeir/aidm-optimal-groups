package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysWithRandomGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMaxFlow;
import nl.tudelft.aidm.optimalgroups.metric.AssignedProjectRank;
import nl.tudelft.aidm.optimalgroups.metric.GroupPreferenceSatisfaction;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.entity.Agents;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;
import nl.tudelft.aidm.optimalgroups.model.entity.Projects;
import org.sql2o.GenericDatasource;

import javax.sql.DataSource;

public class StudentProjectMaxflowExperiment
{
	public static void main()
	{
		DataSource dataSource;

		if (true)
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/test", "henk", "henk");
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
		StudentProjectMaxFlow maxFlow = new StudentProjectMaxFlow(agents, projects);

		//Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxflow.result();
		Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxFlow.result();

		for (var match : matching.asList()) {
			AssignedProjectRank assignedProjectRank = new AssignedProjectRank(match);

			int rankNumber = assignedProjectRank.groupRank();
			System.out.println("Group " + match.from().groupId() + " got project " + match.to().belongingToProject().id() + " (ranked as number " + rankNumber + ")");

			assignedProjectRank.studentRanks().forEach(metric -> {
				System.out.printf("\t\t-Student %s", metric.student().name);
				System.out.printf(", rank: %s", metric.studentsRank());

				System.out.printf("\t\t group satisfaction: %s\n", new GroupPreferenceSatisfaction(match, metric.student()).asFraction());
			});
		}
	}
}
