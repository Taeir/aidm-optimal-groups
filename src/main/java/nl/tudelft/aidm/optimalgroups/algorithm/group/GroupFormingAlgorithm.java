package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.group.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;

public interface GroupFormingAlgorithm extends Groups<Group.FormedGroup>
{
    FormedGroups asFormedGroups();
}
