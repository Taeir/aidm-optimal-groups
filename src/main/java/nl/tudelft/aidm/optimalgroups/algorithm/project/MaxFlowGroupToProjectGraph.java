package nl.tudelft.aidm.optimalgroups.algorithm.project;

import edu.princeton.cs.algs4.Graph;
import nl.tudelft.aidm.optimalgroups.model.entity.Groups;
import nl.tudelft.aidm.optimalgroups.model.entity.Projects;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

public class MaxFlowGroupToProjectGraph extends Graph
{
	private final Groups groups;
	private final Projects projects;

	public MaxFlowGroupToProjectGraph(Groups groups, Projects projects)
	{
		super(groups.count() + projects.count());

		this.groups = groups;
		this.projects = projects;

		// TODO: create the edges
//		for (var group : groups.asCollection())
//		{
//			ProjectPreference projectPreference = group.projectPreference();
//		}
	}




//	public static MaxFlowGroupToProjectGraph from(Groups groups, Projects projects)
//	{
//		groups.
//	}

}
