package nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp;

import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatchings;
import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMatchings;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreferenceOfAgents;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts *all* students matched to some project into a single group
 */
public class SingleGroupPerProjectMatchings implements GroupProjectMatchings<Group>
{
	private final StudentProjectMatchings studentProjectMatching;

	public SingleGroupPerProjectMatchings(StudentProjectMatchings studentProjectMatching)
	{
		this.studentProjectMatching = studentProjectMatching;
	}

	private List<Match<Group, Project>> result = null;

	@Override
	public List<Match<Group, Project>> asList()
	{
		if (result != null)
			return result;

		ArrayList<Match<Group, Project>> result = new ArrayList<>();

		FormedGroups formedGroups = new FormedGroups();

		studentProjectMatching.groupedByProject().forEach((project, agentsAsList) -> {
			Agents agents = new Agents(agentsAsList);
			Group.TentativeGroup group = new Group.TentativeGroup(agents, ProjectPreferenceOfAgents.aggregateWithGloballyConfiguredAggregationMethod(agents));

			Group.FormedGroup formedGroup = formedGroups.addAsFormed(group);
			result.add(new StudentsToProjectMatch(formedGroup, project));
		});

		this.result = result;
		return result;
	}


//	public Matchings<Group, Project> result()
//	{
//		if (theMatching != null)
//			return theMatching;
//
//		FormedGroups formedGroups = new FormedGroups();
//
//		var resultingMatching = new Matchings.ListBasedMatchings<Group, Project>();
//		for (var x : groupedByProject().entrySet())
//		{
//			// TODO: proper group creation
//			Agents agents = Agents.from(x.getValue());
//			Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(agents, new AverageProjectPreferenceOfAgents(agents));
//			Group.FormedGroup formedGroup = formedGroups.addAsFormed(tentativeGroup);
//
//
//			var match = new Matchings.StudentsToProjectMatch(formedGroup, x.getKey());
//			resultingMatching.add(match);
//		}
//
//		theMatching = resultingMatching;
//
//		return theMatching;
//	}

	private class StudentsToProjectMatch implements Match<Group, Project>
	{
		Group group;
		Project project;

		public StudentsToProjectMatch(Group group, Project project)
		{
			this.group = group;
			this.project = project;
		}

		@Override
		public Group from()
		{
			return group;
		}

		@Override
		public Project to()
		{
			return project;
		}

		public Group group()
		{
			return group;
		}

		public Project project()
		{
			return project;
		}
	}
}
