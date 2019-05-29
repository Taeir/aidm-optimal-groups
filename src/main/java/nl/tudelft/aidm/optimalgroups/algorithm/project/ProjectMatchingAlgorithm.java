package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.entity.Groups;
import nl.tudelft.aidm.optimalgroups.model.entity.Projects;

public interface ProjectMatchingAlgorithm
{
	Matching doMatching(Groups groups, Projects projects);
}
