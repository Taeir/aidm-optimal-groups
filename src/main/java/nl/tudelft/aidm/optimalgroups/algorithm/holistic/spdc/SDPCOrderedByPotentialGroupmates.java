package nl.tudelft.aidm.optimalgroups.algorithm.holistic.spdc;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.ProjectPairings;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.graph.BipartitieAgentsProjectGraph;
import nl.tudelft.aidm.optimalgroups.model.graph.DatasetAsGraph;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.*;
import java.util.stream.Collectors;

public class SDPCOrderedByPotentialGroupmates extends SerialDictatorshipWithProjClosures
{
	public SDPCOrderedByPotentialGroupmates(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		super(
			ordered(agents, ProjectPairings.from(agents, projects, groupSizeConstraint, projects.count()).get().k()),
			projects,
			groupSizeConstraint
		);
	}
//
//	private static int k(DatasetContext datasetContext)
//	{
//		return KProjectAgentsPairing.from(datasetContext.allAgents(), datasetContext.allProjects(), datasetContext.groupSizeConstraint()).k();
//	}

	private static Agents ordered(Agents agents, int k)
	{
		var agentList = new ArrayList<>(agents.asCollection());
		BipartitieAgentsProjectGraph graph = new DatasetAsGraph(agents.datasetContext);

		var sortedList = agentList.stream()
			.map(agent -> {
				if (agent.projectPreference().isCompletelyIndifferent()) {
					return new AgentPotentialGroupmates(agent, agents.count());
				}

				var potentialGroupmates = new HashSet<Agent>();

				var agentEdges = graph.edges().from(agent).stream().filter(edge -> edge.rank() <= k).collect(Collectors.toSet());

				for (var topKEdge : agentEdges) {
					var agentsWithProject = graph.edges().to(topKEdge.w().obj()).stream()
						.filter(e -> e.rank() <= k)
						.filter(e -> e.v().obj() != agent)
						.map(e -> e.v().obj())
						.collect(Collectors.toSet());

					potentialGroupmates.addAll(agentsWithProject);
				}

				return new AgentPotentialGroupmates(agent, potentialGroupmates.size());

			})
			.sorted(Comparator.comparing(AgentPotentialGroupmates::count))
			.map(AgentPotentialGroupmates::agent)
			.collect(Collectors.toList());

		return Agents.from(sortedList);

	}

	private record AgentPotentialGroupmates(Agent agent, int count) {}

}
