package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.*;
import nl.tudelft.aidm.optimalgroups.algorithm.project.*;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import org.sql2o.GenericDatasource;

import javax.sql.DataSource;
import java.util.Collection;

public class Application
{
	public static void main(String[] args)
	{
		DataSource dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/bepsys", "root", "root");

		Agents agents = Agents.from(dataSource, 10);
		Projects projects = Projects.fromDb(dataSource, 10);
		System.out.println("Amount of projects: " + projects.count());

		BepSysWithRandomGroups formedGroups = new BepSysWithRandomGroups(agents, 4, 6);
		MaxFlow maxflow = new MaxFlow(formedGroups.finalFormedGroups(), projects);
		Matching matching = maxflow.result();

		// this could have been easier I feel like, but I couldn't figure it out
		int groupIndex = 1;
		for (var genericMatch : matching.asList()) {
			Matching.GroupToProjectMatch match = (Matching.GroupToProjectMatch) genericMatch;
			int projectId = match.project().id();
			int[] preferences = match.group().projectPreference().asArray();
			int rankNumber = -1;
			for (int i = 0; i < preferences.length; i++) {
				if (preferences[i] == projectId) {
					rankNumber = i + 1;
					break;
				}
			}

			System.out.println("Group " + groupIndex + " got project " + projectId + " (ranked as number " + rankNumber + ")");
			groupIndex++;
		}

		Collection<Group> groups = formedGroups.asCollection();
	}
}
