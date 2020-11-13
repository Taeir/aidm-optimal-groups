package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Objects;
import java.util.Set;

public record ProjectAgentsPairing(int kRank, Project project, Set<Agent> agents, Set<Agent> possibleGroupmates) {

	public ProjectAgentsPairing {
		Objects.requireNonNull(project);
		Objects.requireNonNull(agents);
		Objects.requireNonNull(possibleGroupmates);
	}
}
