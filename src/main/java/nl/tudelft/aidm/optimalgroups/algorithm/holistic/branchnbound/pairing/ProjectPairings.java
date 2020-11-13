package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.model.Edge;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.stream.Stream;

public record ProjectPairings(Collection<ProjectAgentsPairing> pairingsAtK, int k)
{
	public static Optional<ProjectPairings> from(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint, int rankBound)
	{
		Assert.that(agents.count() >= groupSizeConstraint.minSize())
			.orThrowMessage("Cannot determine pairings: given agents cannot even constitute a min-size group");
		// Or: always return Optional.empty?

		var bestSubmissiveKPerAgent = new IdentityHashMap<Agent, Integer>(agents.count());
		var pairingsByK = new ProjectPairings[rankBound + 1];

		for (var project : projects.asCollection())
		{
			final var edgesToProject = new ProjectEdges(rankBound, project);

			agents.forEach(agent -> {
				var rank = agent.projectPreference().rankOf(project)
					.orElse(0); // indifferent agents are ok with everything

				if (rank <= rankBound) {
					var edge = new Edge(agent, project, rank);

					edgesToProject.add(edge);
				}
			});

			edgesToProject.pairingForProject(rankBound, groupSizeConstraint)
				.ifPresent(pairing ->
				{
					int k = pairing.kRank();

					for(var agent : pairing.agents()) {
						bestSubmissiveKPerAgent.merge(agent, k, Math::min);
					}

					if (pairingsByK[k] == null) {
						pairingsByK[k] = new ProjectPairings(new LinkedList<>(), k);
					}

					pairingsByK[k].pairingsAtK().add(pairing);
				});
		}

		var worstK = bestSubmissiveKPerAgent.values().stream().max(Integer::compareTo);

		if (worstK.isPresent()) {
			var pairing = pairingsByK[worstK.get()];
			return Optional.of(pairing);
		}

		return Optional.empty();
	}

}
