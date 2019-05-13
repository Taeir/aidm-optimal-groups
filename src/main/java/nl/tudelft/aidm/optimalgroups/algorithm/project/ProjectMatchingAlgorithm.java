package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.entity.Agents;

public interface ProjectMatchingAlgorithm
{
	Matching doMatching(Agents agents);
}
