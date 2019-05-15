package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.entity.Groups;

public interface ProjectMatchingAlgorithm
{
	Matching doMatching(Groups groups);
}
