package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record KProjectAgentsPairing(Collection<ProjectAgentsPairing> pairingsAtK, int k)
{
	public record Edge(Agent from, Project to, int rank){}

	private static class ProjectEdges
	{
		private Set<Agent>[] edgesPerRankBucket;
		private Project project;

		public ProjectEdges(int expectedMaxRank, Project project)
		{
			this.edgesPerRankBucket = new Set[expectedMaxRank+1];// ArrayList<>(expectedMaxRank);
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

			// Indifferent agents are potential group mates of everyone
			if (edgesPerRankBucket[0] != null) {
				potentialGroupmates.addAll(edgesPerRankBucket[0]);
			}

			for (int rank = 1; rank <= rankBoundInclusive; rank++)
			{
				var agentsWithRank = edgesPerRankBucket[rank];

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

	public static Optional<KProjectAgentsPairing> from(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		Assert.that(/*agents.count() == 0 || */agents.count() >= groupSizeConstraint.minSize())
			.orThrowMessage("Cannot determine pairings: given agents cannot even constitute a min-size group");

		var maxRank = agents.datasetContext.allProjects().count() + 1;

		var worstBestRank = maxRank;
//		var bestPairings = new KProjectAgentsPairing(Collections.emptyList(), maxRank + 1);

		var bestSubmissiveKPerAgent = new IdentityHashMap<Agent, Integer>(agents.count());
		var pairingsByK = new KProjectAgentsPairing[maxRank];

		for (var project : projects.asCollection())
		{
			final var edgesToProject = new ProjectEdges(maxRank, project);

			agents.forEach(agent -> {
				var rank = agent.projectPreference().rankOf(project)
					.orElse(0); // indifferent agents are ok with everything

				var edge = new Edge(agent, project, rank);

				edgesToProject.add(edge);
			});

			var potentialPairingMaybe = edgesToProject.pairingForProject(worstBestRank, groupSizeConstraint);

			if (potentialPairingMaybe.isPresent())
			{
				ProjectAgentsPairing pairing = potentialPairingMaybe.get();
				int k = pairing.kRank();

				for(var agent : pairing.agents()) {
					bestSubmissiveKPerAgent.merge(agent, k, Math::min);
				}

				if (pairingsByK[k] == null) {
					pairingsByK[k] = new KProjectAgentsPairing(new ArrayList<>(), k);
				}

				pairingsByK[k].pairingsAtK().add(pairing);
			}
		}

		var worstK = bestSubmissiveKPerAgent.values().stream().max(Integer::compareTo);

		if (worstK.isPresent()) {
			var pairing = pairingsByK[worstK.get()];
			return Optional.of(pairing);
		}

		return Optional.empty();
	}

	public static Stream<ProjectAgentsPairing> henk(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		Assert.that(/*agents.count() == 0 || */agents.count() >= groupSizeConstraint.minSize())
			.orThrowMessage("Cannot determine pairings: given agents cannot even constitute a min-size group");

		var maxRank = agents.datasetContext.allProjects().count() + 1;

		var worstBestRank = maxRank;
//		var bestPairings = new KProjectAgentsPairing(Collections.emptyList(), maxRank + 1);

//		var bestSubmissiveKPerAgent = new IdentityHashMap<Agent, Integer>(agents.count());
//		var pairingsByK = new KProjectAgentsPairing[maxRank];
		var pairings = new LinkedList<ProjectAgentsPairing>();

		for (var project : projects.asCollection())
		{
			final var edgesToProject = new ProjectEdges(maxRank, project);

			agents.forEach(agent -> {
				var rank = agent.projectPreference().rankOf(project)
					.orElse(0); // indifferent agents are ok with everything

				var edge = new Edge(agent, project, rank);

				edgesToProject.add(edge);
			});

			edgesToProject.pairingForProject(worstBestRank, groupSizeConstraint)
				.ifPresent(pairings::add);
		}

		return pairings.stream();
	}

}
