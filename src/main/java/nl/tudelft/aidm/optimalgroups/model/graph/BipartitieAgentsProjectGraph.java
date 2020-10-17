package nl.tudelft.aidm.optimalgroups.model.graph;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Set;

public interface BipartitieAgentsProjectGraph
{
	record Vertex<T>(int id, T obj) {}

	record WeightedEdge(Vertex<Agent> v, Vertex<Project> w, int rank){}

	static BipartitieAgentsProjectGraph from(DatasetContext datasetContext)
	{
		return new DatasetAsGraph(datasetContext);
	}

	interface Edges
	{
		/**
		 * @return Set of all edges
		 */
		Set<WeightedEdge> all();

		/**
		 * @param agent
		 * @return Projects and agent's ranking thereof as set of edges - or an empty set
		 */

		Set<WeightedEdge> from(Agent agent);

		/**
		 * @param project
		 * @return Agents and their ranking of given project as set of edges - or and empty set
		 */
		Set<WeightedEdge> to(Project project);
	}

	Set<Vertex> vertices();
	Edges edges();
}
