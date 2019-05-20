package nl.tudelft.aidm.optimalgroups.algorithm.project;

import edu.princeton.cs.algs4.Graph;
import nl.tudelft.aidm.optimalgroups.model.entity.Groups;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;
import nl.tudelft.aidm.optimalgroups.model.entity.Projects;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.List;

public class MaxFlowGroupToProjectGraph extends Graph
{
	private final Groups groups;
	private final Projects projects;

	public MaxFlowGroupToProjectGraph(Groups groups, Projects projects)
	{
		super(groups.count() + projects.numTotalSlots());

		this.groups = groups;
		this.projects = projects;

		int groupId = 0;
		groups.forEach(group -> {

		});

		// TODO: create the edges
//		for (var group : groups.asCollection())
//		{
//			ProjectPreference projectPreference = group.projectPreference();
//		}
	}

	/**
	 * A project has 1 or more slots. A group having a preference for a project,
	 * has an edge to each of the slots with same weights
	 */
	static class ProjectSlot
	{
		private static int id = 0;
		private static List<ProjectSlot> projectSlots; // For slots over all projects!

		Project project;

	}




//	public static MaxFlowGroupToProjectGraph from(Groups groups, Projects projects)
//	{
//		groups.
//	}

}
