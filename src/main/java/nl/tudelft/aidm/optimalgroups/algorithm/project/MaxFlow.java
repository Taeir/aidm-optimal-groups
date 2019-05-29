package nl.tudelft.aidm.optimalgroups.algorithm.project;

import edu.princeton.cs.algs4.BipartiteMatching;
import louchtch.graphmatch.matching.MaxFlowMatching;
import louchtch.graphmatch.model.*;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Groups;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;
import nl.tudelft.aidm.optimalgroups.model.entity.Projects;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.support.ImplementMe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaxFlow implements ProjectMatchingAlgorithm
{
	@Override
	public Matching doMatching(Groups groups, Projects projects)
	{
		GroupVertices groupVertices = new GroupVertices();
		ProjectVertices projectVertices = new ProjectVertices(projects);

		ProjectGroupPreferenceEdges projectGroupPreferenceEdges = new ProjectGroupPreferenceEdges(groupVertices, projectVertices);

		// Sick cast https://stackoverflow.com/questions/3246137/java-generics-cannot-cast-listsubclass-to-listsuperclass
		// warning: not very safe, but not catastrophic if lists are not modified
		Vertices<GroupProjectMatching> left = (Vertices<GroupProjectMatching>) (Vertices<? extends GroupProjectMatching>) groupVertices;
		Vertices<GroupProjectMatching> right = (Vertices<GroupProjectMatching>) (Vertices<? extends GroupProjectMatching>) projectVertices;

		MaxFlowMatching<GroupProjectMatching> matching = new MaxFlowMatching<>(new MaxFlowGraph<>(left, right, projectGroupPreferenceEdges), SearchType.MinCost);

	}

	private static class GroupVertices extends Vertices<GroupVertexContent>
	{

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
				projectIdToVerticesMap.put(Integer.decode(project.name()), slotVerticesForProject);

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
						this.add(DirectedWeightedEdge.between(group, projectSlotVertex, rank)); // todo proper weight
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
		private final Group group;

		public GroupVertexContent(Group group)
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
