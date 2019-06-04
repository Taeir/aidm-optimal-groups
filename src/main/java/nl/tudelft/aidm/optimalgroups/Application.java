package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.*;
import nl.tudelft.aidm.optimalgroups.algorithm.project.*;
import nl.tudelft.aidm.optimalgroups.metric.AssignedProjectRank;
import nl.tudelft.aidm.optimalgroups.metric.GroupPreferenceSatisfaction;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import org.sql2o.GenericDatasource;

import javax.sql.DataSource;

public class Application
{
	public static void main(String[] args)
	{
		DataSource dataSource;

		if (true)
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
		else
			dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/bepsys", "root", "root");

		Agents agents = Agents.from(dataSource, 10);
		Projects projects = Projects.fromDb(dataSource, 10);
		System.out.println("Amount of projects: " + projects.count());

		BepSysWithRandomGroups formedGroups = new BepSysWithRandomGroups(agents, 4, 6);
		MaxFlow maxflow = new MaxFlow(formedGroups.finalFormedGroups(), projects);

		Matching<Group.FormedGroup, Project.ProjectSlot> matching = maxflow.result();

		// this could have been easier I feel like, but I couldn't figure it out
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

//		Collection<Group> groups = formedGroups.asCollection();
	}
}
