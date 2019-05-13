package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.entity.Agents;
import nl.tudelft.aidm.optimalgroups.model.entity.Groups;

public interface GroupFormingAlgorithm
{
	Groups makeGroups(Agents agents);
}
