package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.model;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Objects;
import java.util.Set;

public record MatchCandidate(int kRank, Project project, Set<Agent> agents, Set<Agent> possibleGroupmates) {

	public MatchCandidate
	{
		Objects.requireNonNull(project);
		Objects.requireNonNull(agents);
		Objects.requireNonNull(possibleGroupmates);
	}

}
