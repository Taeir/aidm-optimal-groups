package nl.tudelft.aidm.optimalgroups.algorithm.project;

import com.google.ortools.graph.MinCostFlow;
import louchtch.graphmatch.model.*;
import nl.tudelft.aidm.optimalgroups.algorithm.PreferencesToCostFn;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings({"Duplicates"})
public class AgentProjectMaxFlowMatching implements AgentToProjectMatching
{
	static {
		System.loadLibrary("jniortools");
	}

	private static Map<Collection<Project>, AgentProjectMaxFlowMatching> existingResultsCache = new ConcurrentHashMap<>();

	private final DatasetContext datasetContext;
	public final Agents students;
	public final Projects projects;
	private final PreferencesToCostFn preferencesToCostFunction;

	//	private Map<Project.ProjectSlot, List<Agent>> groupedBySlot = null;
	private Map<Project, List<Agent>> groupedByProject = null;
	private List<Match<Agent, Project>> asList = null;
	private Boolean allStudentsAreMatched = null;

	public static void flushCache()
	{
		existingResultsCache = new ConcurrentHashMap<>();
	}

	public static AgentProjectMaxFlowMatching of(DatasetContext datasetContext, Agents students, Projects projects)
	{
		if (existingResultsCache.containsKey(projects.asCollection()) == false) {
			AgentProjectMaxFlowMatching maxflow = new AgentProjectMaxFlowMatching(datasetContext, students, projects);
			existingResultsCache.put(projects.asCollection(), maxflow);

			return maxflow;
		}

		AgentProjectMaxFlowMatching existing = existingResultsCache.get(projects.asCollection());
		if (existing.students.equals(students)) {
			return existing;
		}
		else {
			throw new RuntimeException("Requested a cached StudentsProjectsMaxFlow for previously computed projects, but different student set." +
				"Cache implementation only works on projects and assumes identical studens. Decide how to handle this case first (support proj + studs or simply compute this case without caching).");
		}
	}

	/**
	 * When not all agents or projects must be used in the matching, but the context is still one given by DatasetContext
	 * @param datasetContext
	 * @param students A (subset) of students in datasetContext
	 * @param projects A (subset) of projects in datasetContext
	 */
	public AgentProjectMaxFlowMatching(DatasetContext datasetContext, Agents students, Projects projects)
	{
		this(datasetContext, students, projects, (projectPreference, theProject) -> {
			// If project not present: agent is indifferent or does not want the project,
			// in both cases it's ok to assign maximum cost
			return projectPreference.rankOf(theProject).orElse(datasetContext.allProjects().count());
		});
	}

	/**
	 * When not all agents or projects must be used in the matching, but the context is still one given by DatasetContext.
	 * Furthermore, allows overriding of assignment cost through the {@link PreferencesToCostFn} parameter.
	 * @param datasetContext
	 * @param students A (subset) of students in datasetContext
	 * @param projects A (subset) of projects in datasetContext
	 */
	public AgentProjectMaxFlowMatching(DatasetContext datasetContext, Agents students, Projects projects, PreferencesToCostFn preferencesToCostFunction)
	{
		this.datasetContext = datasetContext;
		this.students = students;
		this.projects = projects;
		this.preferencesToCostFunction = preferencesToCostFunction;
	}

	/**
	 * Returns a MaxFlow-matching on the full given datasetcontext with assignment costs being the ranks
	 * @param datasetContext The datacontext
	 */
	public AgentProjectMaxFlowMatching(DatasetContext datasetContext)
	{
		this(datasetContext, datasetContext.allAgents(), datasetContext.allProjects());
	}

	/**
	 * Returns a MaxFlow-matching on the full given datasetcontext but with a custom cost assigning function
	 * @param datasetContext The datacontext
	 * @param preferencesToCostFunction Custom assignment-cost function
	 */
	public AgentProjectMaxFlowMatching(DatasetContext datasetContext, PreferencesToCostFn preferencesToCostFunction)
	{
		this(datasetContext, datasetContext.allAgents(), datasetContext.allProjects(), preferencesToCostFunction);
	}

	@Override
	public DatasetContext datasetContext()
	{
		return this.datasetContext;
	}

	@Override
	public Map<Project, List<Agent>> groupedByProject()
	{
		init();

		return groupedByProject;
	}

	@Override
	public List<Match<Agent, Project>> asList()
	{
		if (asList != null)
			return asList;

//		ListBasedMatchings listBasedMatching = new ListBasedMatchings<Agent, Project>();

		this.asList = new ArrayList<>();

		groupedByProject().forEach((project, agents) -> {
			agents.forEach(agent -> {
				this.asList.add(new AgentToProjectMatch(agent, project));
			});
		});


		return this.asList;
	}

	public boolean allStudentsAreMatched()
	{
		if (allStudentsAreMatched == null) {
			long studentsMatched = this.groupedByProject.values().stream().mapToLong(Collection::size).sum();
			allStudentsAreMatched = studentsMatched == this.students.count();
		}

		return allStudentsAreMatched;
	}

	private void init()
	{
		// Very simple check: init only once, subsequent calls return directly
		if (this.groupedByProject != null) {
			return;
		}

		MinCostFlow minCostFlow = new MinCostFlow();

		int vertId = 0;
		int source = vertId++;
		int sink = vertId++;

		// Source and Sink do not need to supply/consume more than we have students
		minCostFlow.setNodeSupply(source, students.count());
		minCostFlow.setNodeSupply(sink, -students.count());

		var studentVerts = new StudentVertices(students, vertId);
		vertId += students.count();

		var projectVerts = new ProjectVertices(projects, vertId);

		var studentProjectArcs = new ArrayList<FlowArc>(students.count() * projects.count());

		students.forEach(student -> {
			Integer studentVert = studentVerts.toVertex.get(student);
			minCostFlow.addArcWithCapacityAndUnitCost(source, studentVert, 1, 1);

			var prefs = student.projectPreference();
			student.projectPreference().forEach((Project project, int rank) -> {

				var projectVert = projectVerts.vertex.get(project);

				if (projectVert != null)
				{
					var cost = preferencesToCostFunction.costOfGettingAssigned(prefs, project);

					var arc = minCostFlow.addArcWithCapacityAndUnitCost(studentVert, projectVert, 1, cost);
					studentProjectArcs.add(new FlowArc(student, project, arc));
				}
			});
		});

		int maxGroupSize = datasetContext.groupSizeConstraint().maxSize();
		projects.forEach(project -> {
			int numSlots = project.slots().size();
			int capacity = numSlots * maxGroupSize;
			var projectVert = projectVerts.vertex.get(project);
			minCostFlow.addArcWithCapacityAndUnitCost(projectVert, sink, capacity, 1);
		});

		var status = minCostFlow.solveMaxFlowWithMinCost();

		var groupedByProject = new IdentityHashMap<Project, List<Agent>>(projects.count());
		for (var arc : studentProjectArcs)
		{
			// Not all arcs will have a flow assigned, we only care about those that do (non-zero flow: vertices are matched)
			if (minCostFlow.getFlow(arc.id) == 0) continue;

			groupedByProject.computeIfAbsent(arc.project, __ -> new ArrayList<>())
				.add(arc.student);
		}

		this.groupedByProject = groupedByProject;
	}

	private static class StudentVertices
	{
		Map<Agent, Integer> toVertex;
		Map<Integer, Agent> toAgent;

		public StudentVertices(Agents agents, int id)
		{
			toAgent = new IdentityHashMap<>(agents.count());
			toVertex = new IdentityHashMap<>(agents.count());

			for (var agent : agents.asCollection()) {
				toAgent.put(id, agent);
				toVertex.put(agent, id);
				id++;
			}
		}
	}

	private static class ProjectVertices
	{
		Map<Project, Integer> vertex;
		Map<Integer, Project> project;

		public ProjectVertices(Projects projects, int id)
		{
			this.project = new IdentityHashMap<>(projects.count());
			this.vertex = new IdentityHashMap<>(projects.count());

			for (var project : projects.asCollection()) {
				this.project.put(id, project);
				vertex.put(project, id);
				id++;
			}
		}
	}

	private static record FlowArc(Agent student, Project project, int id)
	{
	}
}

