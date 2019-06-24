package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.Groups;

public interface GroupFormingAlgorithm extends Groups<Group.FormedGroup>
{
    FormedGroups asFormedGroups();
}
