package nl.tudelft.aidm.optimalgroups.model.graph;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.*;
import java.util.function.Consumer;

public class DatasetAsGraph implements Graph
{
	private final DatasetContext datasetContext;

	private final Set<Edge> edges;
	private final Set<Vertex> verticesSet;

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

		this.vertices = new Vertices(numAgents + numProjects);
		this.edges = new HashSet<>(numAgents * numProjects);

//		datasetContext.allProjects().forEach(project -> projectVertices.put(project, vertices.vertexOf(project)));


		datasetContext.allAgents().forEach(agent -> {

			Vertex<Agent> agentVertex = vertices.vertexOf(agent);

			agent.projectPreference().forEach((Project project, int rank) -> {
				Vertex<Project> projectVertex = vertices.vertexOf(project);
if (rank <= 5) edges.add(new Edge(agentVertex, projectVertex, rank));
			});

//			agentVertices.put(agent, agentVertex);
		});

		var verticesArray = vertices.vertices;
		this.verticesSet = new HashSet<>(verticesArray.length);
		verticesSet.addAll(Arrays.asList(verticesArray));
	}

	@Override
	public Set<Edge> edges()
	{
		return Collections.unmodifiableSet(edges);
	}

	@Override
	public Set<Vertex> vertices()
	{
		return Collections.unmodifiableSet(verticesSet);
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
		private final Map<Object, Object> existing = new IdentityHashMap<>();

		public Vertices(int capacity)
		{
			idOfNext = 0;
			vertices = new Vertex[capacity];
		}

		public <T> Vertex<T> vertexOf(int id) {
			// Caller should know the type
			return (Vertex<T>) vertices[id];
		}

		public <T> Vertex<T> vertexOf(T obj)
		{
			//noinspection unchecked -- is safe
			return (Vertex<T>) existing.computeIfAbsent(obj, o -> {
				var vert = new Vertex<>(idOfNext++, o);
				vertices[vert.id()] = vert;
				return vert;
			});
		}
	}
}
