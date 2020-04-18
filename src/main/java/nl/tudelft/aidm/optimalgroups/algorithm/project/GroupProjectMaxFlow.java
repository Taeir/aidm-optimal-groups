package nl.tudelft.aidm.optimalgroups.algorithm.project;

import louchtch.graphmatch.matching.MaxFlowMatching;
import louchtch.graphmatch.model.*;
import nl.tudelft.aidm.optimalgroups.model.*;
import nl.tudelft.aidm.optimalgroups.model.match.FormedGroupToProjectSlotMatch;
import nl.tudelft.aidm.optimalgroups.model.match.FormedGroupToProjectSlotMatchings;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupProjectMaxFlow implements GroupProjectMatchings<Group.FormedGroup>
{
	private final FormedGroups groups;
	private final Projects projects;

	public GroupProjectMaxFlow(FormedGroups groups, Projects projects)
	{
		this.groups = groups;
		this.projects = projects;
	}

	private FormedGroupToProjectSlotMatchings result = null;

	@Override
	public List<Match<Group.FormedGroup, Project>> asList()
	{
		if (result != null)
			return result.toProjectMatchings().asList();

		var result = new FormedGroupToProjectSlotMatchings();

		GroupVertices groupVertices = new GroupVertices(groups);
		ProjectVertices projectVertices = new ProjectVertices(projects);

		ProjectGroupPreferenceEdges projectGroupPreferenceEdges = new ProjectGroupPreferenceEdges(groupVertices, projectVertices);

		// Sick cast https://stackoverflow.com/questions/3246137/java-generics-cannot-cast-listsubclass-to-listsuperclass
		// warning: not very safe, but not catastrophic if lists are not modified
		var left = (Vertices<GroupProjectMatching>) (Vertices<? extends GroupProjectMatching>) groupVertices;
		var right = (Vertices<GroupProjectMatching>) (Vertices<? extends GroupProjectMatching>) projectVertices;

		var matching = new MaxFlowMatching<>(new MaxFlowGraph<>(left, right, projectGroupPreferenceEdges), SearchType.MinCost);
		var matchingAsListOfEdges = matching.asListOfEdges();

		for (Edge<GroupProjectMatching> matchEdge : matchingAsListOfEdges)
		{
			Group.FormedGroup group = ((GroupVertexContent) matchEdge.from.content()).group;
			Project.ProjectSlot project = ((ProjectVertexContent) matchEdge.to.content()).slot;

			var match = new FormedGroupToProjectSlotMatch(group, project);
			result.add(match);
		}

		this.result = result;

		return result.toProjectMatchings().asList();
	}

	private static class GroupVertices extends Vertices<GroupVertexContent>
	{
		public GroupVertices(FormedGroups groups)
		{
			groups.forEach(group -> {
				this.listOfVertices.add(new Vertex<>(new GroupVertexContent(group)));
			});
		}
	}

	private static class ProjectVertices extends Vertices<ProjectVertexContent>
	{
		// map to speed up lookups
		private final Map<Integer, List<Vertex<ProjectVertexContent>>> projectIdToVerticesMap = new HashMap<>();

		public ProjectVertices(Projects projects)
		{
			projects.forEach(project -> {
				List<Vertex<ProjectVertexContent>> slotVerticesForProject = new ArrayList<>();

				// fixme: ProjectName is ProjectId but as a string. This wasn't such a good idea in retrospect, should have stuck with objects
				projectIdToVerticesMap.put(project.id(), slotVerticesForProject);

				project.slots().stream()
					.map(projectSlot -> new Vertex<>(new ProjectVertexContent(projectSlot)))
					.forEach(slotVertex -> {
						this.listOfVertices.add(slotVertex);
						slotVerticesForProject.add(slotVertex);
					});
			});
		}

		public List<Vertex<ProjectVertexContent>> slotVerticesForProject(int projectId)
		{
			return projectIdToVerticesMap.get(projectId);
		}
	}

	private static class ProjectGroupPreferenceEdges extends DirectedWeightedEdges<GroupProjectMatching>
	{
		public ProjectGroupPreferenceEdges(GroupVertices groups, ProjectVertices projects)
		{
			groups.forEach(group -> {

				ProjectPreference projectPreference = group.content().projectPreference();
				projectPreference.forEach((projectId, rank) -> {
					List<Vertex<ProjectVertexContent>> projectSlotVertices = projects.slotVerticesForProject(projectId);

					projectSlotVertices.forEach(projectSlotVertex -> {
						this.add(DirectedWeightedEdge.between(group, projectSlotVertex, rank));
					});
				});

			});
		}
	}

	private enum VertexType { GROUP, PROJECT }

	private static class GroupProjectMatching
	{
		VertexType type;
	}

	private static class GroupVertexContent extends GroupProjectMatching
	{
		private final Group.FormedGroup group;

		public GroupVertexContent(Group.FormedGroup group)
		{
			this.group = group;
			this.type = VertexType.GROUP;
		}

		public ProjectPreference projectPreference()
		{
			return group.projectPreference();
		}
	}

	private static class ProjectVertexContent extends GroupProjectMatching
	{
		private final Project.ProjectSlot slot;

		public ProjectVertexContent(Project.ProjectSlot slot)
		{
			this.slot = slot;
			this.type = VertexType.PROJECT;
		}
	}

	/**
	 * Course edition has projects,
	 * Students have preferences over the projects for a course editon
	 *
	 * Each project can have 'max_number_of_groups' groups
	 *
	 *
	 * left: array of group id's
	 * right: project id's (note: projects have spots for multiple groups!)
	 *
	 * determine group preference for each group
	 *
	 * create edges between groups and projects with weight the priority (smaller numbers are higher prio)
	 *     if preference is 0, use a very high weight
	 *
	 * run GraphMatch with minimize cost
	 *
	 */
}
