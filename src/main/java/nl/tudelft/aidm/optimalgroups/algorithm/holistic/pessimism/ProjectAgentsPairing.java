package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Objects;
import java.util.Set;

record ProjectAgentsPairing(Project project, Set<Agent>agents, Set<Agent> possibleGroupmates) {
	public ProjectAgentsPairing {
		Objects.requireNonNull(project);
		Objects.requireNonNull(agents);
		Objects.requireNonNull(possibleGroupmates);
	}
}
