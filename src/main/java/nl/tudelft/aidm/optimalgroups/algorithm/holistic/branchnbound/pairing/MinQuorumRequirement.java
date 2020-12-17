package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.model.project.Project;

@FunctionalInterface
public interface MinQuorumRequirement
{
	NumAgentsTillQuorum forProject(Project project);
}
