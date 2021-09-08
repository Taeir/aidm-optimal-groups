package nl.tudelft.aidm.optimalgroups.model.graph;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.stream.Collectors;

public class DatasetAsGraph implements BipartitieAgentsProjectGraph
{
	private final DatasetContext datasetContext;

	private final Edges edges;
	private final Vertices vertices;

//	private final IdentityHashMap<Agent, Vertex<Agent>> agentVertices;
//	private final IdentityHashMap<Project, Vertex<Project>> projectVertices;

	public DatasetAsGraph(DatasetContext datasetContext)
	{
		Objects.requireNonNull(datasetContext);
		this.datasetContext = datasetContext;

//		this.agentVertices = new IdentityHashMap<>();
//		this.projectVertices = new IdentityHashMap<>();

		int numAgents = datasetContext.allAgents().count();
		int numProjects = datasetContext.allProjects().count();

		this.vertices = new Vertices(numAgents + numProjects+1);
		this.edges = new DatasetEdges();

//		datasetContext.allProjects().forEach(project -> projectVertices.put(project, vertices.vertexOf(project)));
	}

	@Override
	public Edges edges()
	{
		return edges;
	}

	@Override
	public Set<Vertex> vertices()
	{
		return vertices.asSet();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DatasetAsGraph that = (DatasetAsGraph) o;
		return datasetContext.equals(that.datasetContext);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(datasetContext);
	}

	private static class Vertices
	{
		private int idOfNext;
		private final Vertex[] vertices;
		private final Set<Vertex> asSet;
		private final Map<Object, Object> existing = new IdentityHashMap<>();

		public Vertices(int capacity)
		{
			idOfNext = 0;
			vertices = new Vertex[capacity];
			asSet = new HashSet<>(capacity);
		}

		public <T> Vertex<T> vertexOf(int id) {
			// Caller should know the type
			return (Vertex<T>) vertices[id];
		}

		public synchronized <T> Vertex<T> vertexOf(T obj)
		{
			//noinspection unchecked -- is safe
			return (Vertex<T>) existing.computeIfAbsent(obj, o -> {
				var vert = new Vertex<>(idOfNext, o);

				idOfNext += 1;

				if (vert.id() >= vertices.length) {

					var zzz = existing.keySet().stream().sorted(Comparator.comparing(x -> x instanceof Agent ? ((Agent) x).sequenceNumber * 1000 : ((Project) x).sequenceNum())).collect(Collectors.toList());

					return null;
				}

				Assert.that(vertices[vert.id()] == null).orThrowMessage("BugCheck: vertex already exists...");

				vertices[vert.id()] = vert;
				asSet.add(vert);
				return vert;
			});
		}

		public Set<Vertex> asSet()
		{
			return Collections.unmodifiableSet(asSet);
		}
	}

	public class DatasetEdges implements Edges
	{
		private Set<WeightedEdge> edges;

		private Map<Agent, Set<WeightedEdge>> edgesFromAgent;
		private Map<Project, Set<WeightedEdge>> edgesToProject;

		public DatasetEdges()
		{
			edges = new HashSet<>(datasetContext.allAgents().count() * datasetContext.allProjects().count());
			edgesFromAgent = new IdentityHashMap<>(datasetContext.allProjects().count());
			edgesToProject = new IdentityHashMap<>(datasetContext.allAgents().count());

			datasetContext.allAgents().forEach(agent ->
			{
				Vertex<Agent> agentVertex = vertices.vertexOf(agent);

				agent.projectPreference().forEach((Project project, RankInPref rank) -> {
					Vertex<Project> projectVertex = vertices.vertexOf(project);

					if (rank.unacceptable())
						return true; // skip - will never happen, the proj is in pref!

					// Assign w = 1 to all projects of indifferent agents, w = 0 might cause an algo to try these too much (but should be otherwise fine)
					// and anything heavier will just inflate the obj function. So assume every project is their 1st choice.
					int weight = rank.isCompletelyIndifferent() ? 1 : rank.asInt();

					WeightedEdge edge = new WeightedEdge(agentVertex, projectVertex, weight);

					edges.add(edge);

					edgesFromAgent.computeIfAbsent(agent, __ -> new HashSet<>()).add(edge);
					edgesToProject.computeIfAbsent(project, __ -> new HashSet<>()).add(edge);
					
					return true; // continue iter
				});
			});
		}

		@Override
		public Set<WeightedEdge> all()
		{
			return Collections.unmodifiableSet(edges);
		}

		@Override
		public Set<WeightedEdge> from(Agent agent)
		{
			return edgesFromAgent.getOrDefault(agent, Collections.emptySet());
		}

		@Override
		public Set<WeightedEdge> to(Project project)
		{
			return edgesToProject.getOrDefault(project, Collections.emptySet());
		}
	}
}
