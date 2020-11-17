package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model.MatchCandidate;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.stream.Collectors;

public record WorstAmongBestProjectPairings(Collection<MatchCandidate> pairingsAtK, int k)
{
	public static Optional<WorstAmongBestProjectPairings> from(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint, int rankBound)
	{
		Assert.that(agents.count() >= groupSizeConstraint.minSize())
			.orThrowMessage("Cannot determine pairings: given agents cannot even constitute a min-size group");
		// Or: always return Optional.empty?

		var bestPerAgent = new IdentityHashMap<Agent, Integer>(agents.count());
		var pairingsByK = new WorstAmongBestProjectPairings[rankBound + 1];

		var bestHumblePairings = new BestHumblePairings(agents, projects, groupSizeConstraint, rankBound);

		for (var project : projects.asCollection())
		{
			bestHumblePairings.forProject(project)
				.ifPresent(pairing ->
				{
					int k = pairing.kRank();

					for(var agent : pairing.agents()) {
						bestPerAgent.merge(agent, k, Math::min);
					}

					if (pairingsByK[k] == null) {
						pairingsByK[k] = new WorstAmongBestProjectPairings(new LinkedList<>(), k);
					}

					pairingsByK[k].pairingsAtK().add(pairing);
				});
		}

		var worstK = bestPerAgent.values().stream().max(Integer::compareTo);

		if (worstK.isPresent()) {
			// These are the pairings with k being the worstK - but not every candidate pairing
			// will contain the agents who actually have that "worstK" (because they were found earlier on)
			var pairing = pairingsByK[worstK.get()];

			// So need to filter out only those candidates that contain agents that have that worstK rank
			var filtered = pairing.pairingsAtK().stream().filter(matchCandidate ->
				matchCandidate.agents().stream().anyMatch(agent -> bestPerAgent.get(agent).equals(worstK.get()))
			).collect(Collectors.toList());

			return Optional.of(new WorstAmongBestProjectPairings(filtered, worstK.get()));
		}

		return Optional.empty();
	}

}
