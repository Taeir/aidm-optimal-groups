package nl.tudelft.aidm.optimalgroups.model.graph;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Set;

public interface Graph
{
	record Vertex<T>(int id, T obj) {}
	record Edge(Vertex<Agent> v, Vertex<Project> w, int rank){}

	static Graph from(DatasetContext datasetContext)
	{
		return new DatasetAsGraph(datasetContext);
	}

	Set<Vertex> vertices();
	Set<Edge> edges();
}
