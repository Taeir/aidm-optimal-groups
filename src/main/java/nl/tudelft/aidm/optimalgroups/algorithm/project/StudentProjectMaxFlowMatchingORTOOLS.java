package nl.tudelft.aidm.optimalgroups.algorithm.project;

import com.google.ortools.graph.MinCostFlow;
import louchtch.graphmatch.model.*;
import nl.tudelft.aidm.optimalgroups.model.entity.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"Duplicates"})
public class StudentProjectMaxFlowMatchingORTOOLS implements StudentProjectMatching //implements GroupProjectMatching
{
	// MAKE CONFIGURABLE WITH GROUP SIZE CONSTRAINTS
//	private static final int MAX_GROUP_SIZE = 6;

	static {
		System.loadLibrary("jniortools");
	}

	private static Map<Collection<Project>, StudentProjectMaxFlowMatchingORTOOLS> existingResultsCache = new ConcurrentHashMap<>();

	// source and sink vertices
	private static Vertex<Object> source = new Vertex<>(null);
	private static Vertex<Object> sink = new Vertex<>(null);


	public final Agents students;
	public final Projects projects;
	public final int maxGroupSize;

	//	private Map<Project.ProjectSlot, List<Agent>> groupedBySlot = null;
	private Map<Project, List<Agent>> groupedByProject = null;


	public static StudentProjectMaxFlowMatchingORTOOLS of(Agents students, Projects projects, int maxGroupSize)
	{
		if (existingResultsCache.containsKey(projects.asCollection()) == false) {
			StudentProjectMaxFlowMatchingORTOOLS maxflow = new StudentProjectMaxFlowMatchingORTOOLS(students, projects, maxGroupSize);
			existingResultsCache.put(projects.asCollection(), maxflow);

			return maxflow;
		}

		StudentProjectMaxFlowMatchingORTOOLS existing = existingResultsCache.get(projects.asCollection());
		if (existing.students != students) { // reference equality suffices
			throw new RuntimeException("Requested a cached StudentsProjectsMaxFlow for previously computed projects, but different student set." +
				"Cache implementation only works on projects and assumes identical studens. Decide how to handle this case first (support proj + studs or simply compute this case without caching).");
		}

		return existing;
	}

	public StudentProjectMaxFlowMatchingORTOOLS(Agents students, Projects projects, int maxGroupSize)
	{
		this.students = students;
		this.projects = projects;
		this.maxGroupSize = maxGroupSize;
	}

	@Override
	public Map<Project, List<Agent>> groupedByProject()
	{
		init();

		return groupedByProject;
	}

	private List<Match<Agent, Project>> asList = null;

	@Override
	public List<Match<Agent, Project>> asList()
	{
		if (asList != null)
			return asList;

//		ListBasedMatching listBasedMatching = new ListBasedMatching<Agent, Project>();

		this.asList = new ArrayList<>();

		groupedByProject().forEach((project, agents) -> {
			agents.forEach(agent -> {
				this.asList.add(new AgentToProjectMatch(agent, project));
			});
		});


		return this.asList;
	}


	// quick and dirty: want access to groupedByProject but also keep the asList() method functioning AND without unnecessary recomputing values each time
	public void init()
	{
		// Very simple check: init only once, subsequent calls return directly
		if (this.groupedByProject != null) {
			return;
		}

		var groupedByProject = new IdentityHashMap<Project, List<Agent>>(projects.count());

		StudentVertices studentVertices = new StudentVertices(students);
		ProjectVertices projectVertices = new ProjectVertices(projects);
		var edges = new StudentToProjectEdges(studentVertices, projectVertices);

		MinCostFlow minCostFlow = new MinCostFlow();

		int source = StudentProjectMaxFlowMatchingORTOOLS.source.id;
		int sink = StudentProjectMaxFlowMatchingORTOOLS.sink.id;

		// Source and Sink do not need to supply/consume more than we have students
		minCostFlow.setNodeSupply(source, studentVertices.count());
		minCostFlow.setNodeSupply(sink, -studentVertices.count());

		studentVertices.forEach(studentVertex -> {
			minCostFlow.addArcWithCapacityAndUnitCost(source, studentVertex.id, 1, 1);
		});

		projectVertices.forEach(projectVertex -> {
			Project project = projectVertex.content().theProject;
			int capacity = project.slots().size() * maxGroupSize;
			minCostFlow.addArcWithCapacityAndUnitCost(projectVertex.id, sink, capacity, 1);
		});


		List<Integer> arcs = new ArrayList<>();

		edges.forEach(edge -> {
			// Convert the edge to an OrTools MaxFlow arc with capacity 1 and cost set to the weight of the edge (the rank of the preference this edge represents)
			int arc = minCostFlow.addArcWithCapacityAndUnitCost(edge.from.id, edge.to.id, 1, edge.weight);

			// We need to record the id's of the arcs created by OrTools MaxFlow implementation
			// as we need to query them after the problem instance was solved.
			arcs.add(arc);
		});

		// TODO: check if status (return value) is always "OPTIMAL"?
		minCostFlow.solveMaxFlowWithMinCost();

		for (var arc : arcs)
		{
			// Not all arcs will have a flow assigned, we only care about those that do (non-zero flow: vertices are matched)
			if (minCostFlow.getFlow(arc) == 0) continue;

			int from = minCostFlow.getTail(arc);
			int to = minCostFlow.getHead(arc);

			// Not very efficient: getting the agent/project instance for the corresponding ID
			Agent student = studentVertices.asReadonlyList().stream().filter(v -> v.id == from).findAny().get().content().theStudent;
			Project project = projectVertices.asReadonlyList().stream().filter(v -> v.id == to).findAny().get().content().theProject;

			// Put in the grouping
			groupedByProject.computeIfAbsent(project, __ -> new ArrayList<>()).add(student);
		}

		this.groupedByProject = groupedByProject;
	}

	///////////
	/* EDGES */
	///////////
	private static class StudentToProjectEdges extends DirectedWeightedEdges<StudentProjectMatchingVertexContent> // no generic, we'll cast
	{
		public StudentToProjectEdges(StudentVertices studentVertices, ProjectVertices projectVertices)
		{
			// for each student
			studentVertices.forEach(studentVertex -> {

				// for each student's preference, create the edges to projects according to preferences
				Agent student = studentVertex.content().theStudent;

				if (student.projectPreference.isCompletelyIndifferent()) {
					// Indifferent -> no projects in pref profile at all
					projectVertices.forEach(projectVertex -> {
						// This student is indifferent, therefore prioritize everyone else by assigning lowest rank
						// the "combined preferences" algorithm does the same. Another approach: exclude these studens from
						// the maxflow matching and only add them at the very end as "wildcard" students
						// TODO: investigate effects
						int rank = projectVertices.count() - 1;

						var edge = DirectedWeightedEdge.between(studentVertex, projectVertex, rank);
						add(edge);
					});
				}
				else {
					// Note: if student is missing projects from the profile, no edge will be created
					// therefore projects that are missing from the pref profile are counted as "do not want"
					student.projectPreference.forEach((projectId, rank) -> {
						projectVertices.findForProject(projectId)
							.ifPresent(projectVertex -> {
								var edge = DirectedWeightedEdge.between(studentVertex, projectVertex, rank);
								add(edge);
							});
					});

				}
			});
		}
	}

	//////////////
	/* VERTICES */
	//////////////
	private static class ProjectVertices extends Vertices<ProjectVC>
	{
		private Map<Integer, Vertex<ProjectVC>> projectIdToProjectVert;

		public ProjectVertices(Projects projects)
		{
			projectIdToProjectVert = new HashMap<>();

			projects.asCollection().forEach(project -> {
				var vert = new Vertex<>(new ProjectVC(project));

				this.listOfVertices.add(vert);
				this.projectIdToProjectVert.put(project.id(), vert);
			});
		}

		public Optional<Vertex<ProjectVC>> findForProject(int projectId)
		{
			Vertex<ProjectVC> value = projectIdToProjectVert.get(projectId);
			return Optional.ofNullable(value);
		}
	}

	private static class StudentVertices extends Vertices<StudentVC>
	{
		public StudentVertices(Agents students)
		{
			students.forEach(student -> {
				var vert = new Vertex<>(new StudentVC(student));
				listOfVertices.add(vert);
			});
		}
	}

	/////////////////////
	/* VERTEX CONTENTS */
	/////////////////////
	private interface StudentProjectMatchingVertexContent {}

	private static class ProjectVC implements StudentProjectMatchingVertexContent
	{
		final Project theProject;

		public ProjectVC(Project theProject)
		{
			this.theProject = theProject;
		}
	}

	private static class StudentVC implements StudentProjectMatchingVertexContent
	{
		final Agent theStudent;

		public StudentVC(Agent theStudent)
		{
			this.theStudent = theStudent;
		}
	}
}

