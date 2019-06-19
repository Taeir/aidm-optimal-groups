package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreferenceOfAgents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SingleGroupPerProjectMatching implements GroupProjectMatching<Group>
{
	private final StudentProjectMatching studentProjectMatching;

	public SingleGroupPerProjectMatching(StudentProjectMatching studentProjectMatching)
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
			Group.TentativeGroup group = new Group.TentativeGroup(agents, ProjectPreferenceOfAgents.getChosenMethod(agents));

			Group.FormedGroup formedGroup = formedGroups.addAsFormed(group);
			result.add(new StudentsToProjectMatch(formedGroup, project));
		});

		this.result = result;
		return result;
	}


//	public Matching<Group, Project> result()
//	{
//		if (theMatching != null)
//			return theMatching;
//
//		FormedGroups formedGroups = new FormedGroups();
//
//		var resultingMatching = new Matching.ListBasedMatching<Group, Project>();
//		for (var x : groupedByProject().entrySet())
//		{
//			// TODO: proper group creation
//			Agents agents = Agents.from(x.getValue());
//			Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(agents, new AverageProjectPreferenceOfAgents(agents));
//			Group.FormedGroup formedGroup = formedGroups.addAsFormed(tentativeGroup);
//
//
//			var match = new Matching.StudentsToProjectMatch(formedGroup, x.getKey());
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
