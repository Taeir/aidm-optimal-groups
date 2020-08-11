package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.group.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;

/**
 * A group forming algorithm is a way of making groups from Agents (and whatever else it requires)
 */
public interface GroupFormingAlgorithm extends Groups<Group.FormedGroup>
{
    FormedGroups asFormedGroups();
}
