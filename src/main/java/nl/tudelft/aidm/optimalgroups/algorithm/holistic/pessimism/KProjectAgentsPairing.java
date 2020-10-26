package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public record KProjectAgentsPairing(Collection<ProjectAgentsPairing> pairingsAtK, int k)
{
	public record Edge(Agent from, Project to, int rank){}

	private static class ProjectEdges
	{
		private Set<Agent>[] edgesPerRankBucket;
		private Project project;

		public ProjectEdges(int expectedMaxRank, Project project)
		{
			this.edgesPerRankBucket = new Set[expectedMaxRank];// ArrayList<>(expectedMaxRank);
			this.project = project;
		}

		public void add(Edge edge)
		{
			var bin = edgesPerRankBucket[edge.rank];
			if (bin == null) {
				bin = new HashSet<>();
				edgesPerRankBucket[edge.rank] = bin;
			}

			bin.add(edge.from);
		}

		public Optional<ProjectAgentsPairing> pairingForProject(int rankBoundInclusive, GroupSizeConstraint groupSizeConstraint)
		{
			var potentialGroupmates = new HashSet<Agent>();

			for (int rank = 1; rank <= rankBoundInclusive; rank++)
			{
				int indexOfRank = rank - 1;
				var agentsWithRank = edgesPerRankBucket[indexOfRank];

				if (agentsWithRank == null) continue;

				if (agentsWithRank.size() + potentialGroupmates.size() >= groupSizeConstraint.minSize()) {
					// We've reached the "pivot point", so the 'rank' is the 'k'
					return Optional.of(new ProjectAgentsPairing(rank, project, agentsWithRank, potentialGroupmates));
				}

				potentialGroupmates.addAll(agentsWithRank);
			}

			return Optional.empty();
		}
	}

	public static KProjectAgentsPairing from(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		var maxRank = agents.datasetContext.allProjects().count() + 1;

		var bestPairings = new KProjectAgentsPairing(Collections.emptyList(), maxRank + 1);

		for (var project : projects.asCollection())
		{
			final var edgesToProject = new ProjectEdges(maxRank, project);

			agents.forEach(agent -> {
				var rank = agent.projectPreference().rankOf(project).orElse(1);
				var edge = new Edge(agent, project, rank);

				edgesToProject.add(edge);
			});

			var potentialPairing = edgesToProject.pairingForProject(bestPairings.k(), groupSizeConstraint);

			if (potentialPairing.isPresent()) {
				ProjectAgentsPairing pairing = potentialPairing.get();

				if (bestPairings.k() > pairing.kRank()) {
					var pairings = new ArrayList<ProjectAgentsPairing>();
					pairings.add(pairing);

					bestPairings = new KProjectAgentsPairing(pairings, pairing.kRank());
				}

				else if (bestPairings.k() == pairing.kRank()) {
					bestPairings.pairingsAtK().add(pairing);
				}
			}
		}

		return bestPairings;
	}

}
