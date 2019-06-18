package nl.tudelft.aidm.optimalgroups.algorithm.project;

import com.google.ortools.graph.MinCostFlow;
import louchtch.graphmatch.matching.MaxFlowMatching;
import louchtch.graphmatch.model.*;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import nl.tudelft.aidm.optimalgroups.model.pref.AverageProjectPreferenceOfAgents;

import java.util.*;

@SuppressWarnings({"Duplicates"})
public class StudentProjectMaxFlowORTOOLS //implements ProjectMatchingAlgorithm
{
	static {
		System.loadLibrary("jniortools");
	}

	Agents students;
	Projects projects;

	private Map<Project.ProjectSlot, List<Agent>> groupedBySlot = null;
	private Map<Project, List<Agent>> groupedByProject = null;

	private static Map<Projects, StudentProjectMaxFlowORTOOLS> existingResultsCache = new WeakHashMap<>();

	public static StudentProjectMaxFlowORTOOLS of(Agents students, Projects projects)
	{
		if (existingResultsCache.containsKey(projects) == false) {
			StudentProjectMaxFlowORTOOLS maxflow = new StudentProjectMaxFlowORTOOLS(students, projects);
			existingResultsCache.put(projects, maxflow);

			return maxflow;
		}

		StudentProjectMaxFlowORTOOLS existing = existingResultsCache.get(projects);
		if (existing.students != students) {
			throw new RuntimeException("Requested a cached StudentsProjectsMaxFlow for previously computed projects, but different student set." +
				"Cache implementation only works on projects and assumes identical studens. Decide how to handle this case first (support proj + studs or simply compute this case without caching).");
		}

		return existing;
	}

	private StudentProjectMaxFlowORTOOLS(Agents students, Projects projects)
	{
		this.students = students;
		this.projects = projects;
	}

	private Matching.FormedGroupToProjectMatchings theMatching = null;
	public Matching.FormedGroupToProjectMatchings result()
	{
		init();

		if (theMatching != null)
			return theMatching;

		FormedGroups formedGroups = new FormedGroups();

		var resultingMatching = new Matching.FormedGroupToProjectMatchings();
		for (var x : groupedBySlot.entrySet())
		{
			Agents agents = Agents.from(x.getValue());
			Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(agents, new AverageProjectPreferenceOfAgents(agents));
			Group.FormedGroup formedGroup = formedGroups.addAsFormed(tentativeGroup);


			var match = new Matching.FormedGroupToProjectSlotMatch(formedGroup, x.getKey());
			resultingMatching.add(match);
		}

		theMatching = resultingMatching;

		return theMatching;
	}

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


		MinCostFlow minCostFlow = new MinCostFlow();

//		int source = Integer.MAX_VALUE - 1;
//		int sink = Integer.MAX_VALUE;

		int source = studentVertices.count() * projectSlotVertices.count();
		int sink = source + 1;

		minCostFlow.setNodeSupply(source, studentVertices.count());
		minCostFlow.setNodeSupply(sink, -studentVertices.count());

		left.forEach(studentVertex -> {
			int result = minCostFlow.addArcWithCapacityAndUnitCost(source, studentVertex.id, 1, 1);

			if (result < 0) {
				System.out.println("Adding edge from source " + source + " -> " + studentVertex.id);
				throw new RuntimeException("Boe 1");
			}
//			System.out.println("Adding edge from source " + source + " -> " + studentVertex.id);
//			minCostFlow.setNodeSupply(studentVertex.id, 0);
		});

		right.forEach(slotVertex -> {
			int result = minCostFlow.addArcWithCapacityAndUnitCost(slotVertex.id, sink, 1, 1);

			if (result < 0) {
				System.out.println("Adding edge from slot " + slotVertex.id + " -> sink " + sink);
				throw new RuntimeException("Boe 2");
			}
//			minCostFlow.setNodeSupply(slotVertex.id, 0);
		});

		List<Integer> arcs = new ArrayList<>();
		edges.forEach(edge -> {
			int arc = minCostFlow.addArcWithCapacityAndUnitCost(edge.from.id, edge.to.id, 1, edge.weight);
			arcs.add(arc);

			studentVertices.asReadonlyList().stream().filter(s -> s.id == edge.from.id).findAny().get();
			projectSlotVertices.asReadonlyList().stream().filter(ps -> ps.id == edge.to.id).findAny().get();

		});

		var status = minCostFlow.solveMaxFlowWithMinCost();

		for (var arc : arcs)
		{
			if (minCostFlow.getFlow(arc) == 0) continue;

			int from = minCostFlow.getTail(arc);
			int to = minCostFlow.getHead(arc);

			Agent student = studentVertices.asReadonlyList().stream().filter(v -> v.id == from).findAny().get().content().theStudent;
			Project project = projectSlotVertices.asReadonlyList().stream().filter(v -> v.id == to).findAny().get().content().theProjectSlot.belongingToProject();

			//groupedBySlot.computeIfAbsent(student, __ -> new ArrayList<>()).add(student);
			groupedByProject.computeIfAbsent(project, __ -> new ArrayList<>()).add(student);
		}



//		var graph = new MaxFlowGraph<StudentProjectMatchingVertexContent>(left, right, edges);
//		var matchingd = new MaxFlowMatching<>(graph, SearchType.MinCost);

//		minCostResult.asList().forEach(edge -> {
//			Project.ProjectSlot projectSlot = ((ProjectSlotStudentSlotContent) edge.to.content()).theProjectSlot;
//			Agent student = ((StudentVertexContent) edge.from.content()).theStudent;
//
//
//		});
	}

	///////////
	/* EDGES */
	///////////
	private static class StudentToProjectSlotsEdges extends DirectedWeightedEdges<StudentProjectMatchingVertexContent> // no generic, we'll cast
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

