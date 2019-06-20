package nl.tudelft.aidm.optimalgroups.algorithm.project;

import louchtch.graphmatch.matching.MaxFlowMatching;
import louchtch.graphmatch.model.*;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreferenceOfAgents;

import java.util.*;

@SuppressWarnings("Duplicates")
public class StudentProjectMaxFlow implements StudentProjectMatching //implements GroupProjectMatching
{
	Agents students;
	Projects projects;

	private Map<Project.ProjectSlot, List<Agent>> groupedBySlot = null;
	private Map<Project, List<Agent>> groupedByProject = null;

	private static Map<Projects, StudentProjectMaxFlow> existingResultsCache = new WeakHashMap<>();

	public static StudentProjectMaxFlow of(Agents students, Projects projects)
	{
		if (existingResultsCache.containsKey(projects) == false) {
			StudentProjectMaxFlow maxflow = new StudentProjectMaxFlow(students, projects);
			existingResultsCache.put(projects, maxflow);

			return maxflow;
		}

		StudentProjectMaxFlow existing = existingResultsCache.get(projects);
		if (existing.students != students) {
			throw new RuntimeException("Requested a cached StudentsProjectsMaxFlow for previously computed projects, but different student set." +
				"Cache implementation only works on projects and assumes identical studens. Decide how to handle this case first (support proj + studs or simply compute this case without caching).");
		}

		return existing;
	}

	private StudentProjectMaxFlow(Agents students, Projects projects)
	{
		this.students = students;
		this.projects = projects;
	}

	private FormedGroupToProjectSlotMatchings theMatching = null;
	public FormedGroupToProjectSlotMatchings result()
	{
		init();

		if (theMatching != null)
			return theMatching;

		FormedGroups formedGroups = new FormedGroups();

		var resultingMatching = new FormedGroupToProjectSlotMatchings();
		for (var x : groupedBySlot.entrySet())
		{
			Agents agents = Agents.from(x.getValue());
			Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(agents, ProjectPreferenceOfAgents.aggregateWithGloballyConfiguredAggregationMethod(agents));
			Group.FormedGroup formedGroup = formedGroups.addAsFormed(tentativeGroup);


			var match = new Matching.FormedGroupToProjectSlotMatch(formedGroup, x.getKey());
			resultingMatching.add(match);
		}

		theMatching = resultingMatching;

		return theMatching;
	}


	private List<Match<Agent, Project>> result = null;

	@Override
	public List<Match<Agent, Project>> asList()
	{
		if (result != null)
			return result;

//		ListBasedMatching listBasedMatching = new ListBasedMatching<Agent, Project>();

		Map<Project, List<Agent>> groupedByProject = groupedByProject();

		result = new ArrayList<>();

		groupedByProject.forEach((project, agents) -> {
			agents.forEach(agent -> {
				result.add(new AgentToProjectMatch(agent, project));
			});
		});


		return result;
	}

	@Override
	public Map<Project, List<Agent>> groupedByProject()
	{
		init();

		return groupedByProject;
	}

	// quick and dirty: want access to groupedByProject but also keep the result() method functioning AND without unnecessary recomputing values each time
	public void init()
	{
		if (groupedByProject != null && groupedBySlot != null)
			return;


		groupedBySlot = new HashMap<Project.ProjectSlot, List<Agent>>(projects.countAllSlots());
		groupedByProject = new HashMap<Project, List<Agent>>(projects.count());

		StudentVertices studentVertices = new StudentVertices(students);
		ProjectStudentSlotVertices projectSlotVertices = new ProjectStudentSlotVertices(projects);
		var edges = new StudentToProjectSlotsEdges(studentVertices, projectSlotVertices);

		// Sick cast https://stackoverflow.com/questions/3246137/java-generics-cannot-cast-listsubclass-to-listsuperclass
		// warning: not very safe, but not catastrophic if lists are not modified
		var left = (Vertices<StudentProjectMatchingVertexContent>) (Vertices<? extends StudentProjectMatchingVertexContent>) studentVertices;
		var right = (Vertices<StudentProjectMatchingVertexContent>) (Vertices<? extends StudentProjectMatchingVertexContent>) projectSlotVertices;

		var graph = new MaxFlowGraph<StudentProjectMatchingVertexContent>(left, right, edges);

		var matching = new MaxFlowMatching<>(graph, SearchType.MinCost);

		matching.asListOfEdges().forEach(edge -> {
			Project.ProjectSlot projectSlot = ((ProjectSlotStudentSlotContent) edge.to.content()).theProjectSlot;
			Agent student = ((StudentVertexContent) edge.from.content()).theStudent;

			groupedBySlot.computeIfAbsent(projectSlot, __ -> new ArrayList<>()).add(student);
			groupedByProject.computeIfAbsent(projectSlot.belongingToProject(), __ -> new ArrayList<>()).add(student);
		});
	}

	///////////
	/* EDGES */
	///////////
	private static class StudentToProjectSlotsEdges extends DirectedWeightedEdges // no generic, we'll cast
	{
		public StudentToProjectSlotsEdges(StudentVertices studentVertices, ProjectStudentSlotVertices projectSlotVertices)
		{
			// for each student
			studentVertices.forEach(studentVertex -> {

				// for each student's preference
				studentVertex.content().theStudent.projectPreference.forEach((projectId, rank) -> {

					// Find the corresponding project-slot vertices
					projectSlotVertices.findAllForProject(projectId).forEach(projSlotVert -> {

						// and create/add and edge between them
						var edge = DirectedWeightedEdge.between(studentVertex, projSlotVert, rank);
						add(edge);
					});
				});

			});
		}
	}

	//////////////
	/* VERTICES */
	//////////////
	private static class ProjectStudentSlotVertices extends Vertices<ProjectSlotStudentSlotContent>
	{
		private Map<Integer, List<Vertex<ProjectSlotStudentSlotContent>>> projectToSlots;

		public ProjectStudentSlotVertices(Projects projects)
		{
			projectToSlots = new HashMap<>();

			projects.asCollection().forEach(project -> {
				project.slots().forEach(projectSlot -> {

					for (int i = 0; i < 6; i++)
					{
						var vert = new Vertex<>(new ProjectSlotStudentSlotContent(projectSlot));
						this.listOfVertices.add(vert);

						projectToSlots
							.computeIfAbsent(project.id(), __ -> new ArrayList<>(project.slots().size()))
							.add(vert);
					}
				});
			});
		}

		public Collection<Vertex<ProjectSlotStudentSlotContent>> findAllForProject(int projectIdToFind)
		{
			return projectToSlots.getOrDefault(projectIdToFind, Collections.emptyList());
		}
	}

	private static class StudentVertices extends Vertices<StudentVertexContent>
	{
		public StudentVertices(Agents students)
		{
			students.forEach(student -> {
				var vert = new Vertex<>(new StudentVertexContent(student));
				listOfVertices.add(vert);
			});
		}
	}

	/////////////////////
	/* VERTEX CONTENTS */
	/////////////////////
	private interface StudentProjectMatchingVertexContent {}

	private static class ProjectSlotStudentSlotContent implements StudentProjectMatchingVertexContent
	{
		final Project.ProjectSlot theProjectSlot;

		public ProjectSlotStudentSlotContent(Project.ProjectSlot theProjectSlot)
		{
			this.theProjectSlot = theProjectSlot;
		}
	}

	private static class StudentVertexContent implements StudentProjectMatchingVertexContent
	{
		final Agent theStudent;

		public StudentVertexContent(Agent theStudent)
		{
			this.theStudent = theStudent;
		}
	}
}
