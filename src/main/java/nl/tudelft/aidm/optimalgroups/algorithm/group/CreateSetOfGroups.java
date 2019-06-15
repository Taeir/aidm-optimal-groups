package nl.tudelft.aidm.optimalgroups.algorithm.group;

import java.util.HashMap;
import java.util.Map;

final public class CreateSetOfGroups
{
    private CreateSetOfGroups()
    {}

    /**
     * Given an amount of students and group size constraints, calculate a possible set of group sizes
     * The algorithm works from max size downwards, preferring to create groups with as many students as possible
     * @Return A HashMap where the key is the size of a group and the value the amount of groups with that size
     */
    static public Map<Integer, Integer> CreateSetOfGroups(int nrStudents, int minGroupSize, int maxGroupSize)
    {
        Map<Integer, Integer> groupSizes = new HashMap<Integer, Integer>(); //Key = size of group, Value = amount of groups with that size
        boolean matchingDone = false;
        while(!matchingDone)
        {
            int nrGroupsWithMaxSize = nrStudents / maxGroupSize; //Create as many groups of max size as possible
            int remainder = nrStudents % maxGroupSize; // Remaining students after creating as much groups with max size
            if(remainder == 0) //if no remaining students
            {
                groupSizes.put(maxGroupSize, nrGroupsWithMaxSize); //students are perfect fit for max group size
                matchingDone = true;
            }
            else if(minGroupSize == maxGroupSize) //if there are remainders with a solo size constraint, impossible matching
            {
                throw new RuntimeException("Impossible group matching problem, can't create a set of group sizes with current amount of students and group constraints");
            }
            else if(remainder >= minGroupSize) //check if remainder fits in group size constraints
            {
                groupSizes.put(maxGroupSize, nrGroupsWithMaxSize); //create as much groups with max size possible
                groupSizes.put(remainder, 1); //then create a group with size of remainders
                matchingDone = true;
            }
            else //check if remainder can steal enough from other groups to match size constraints
            {
                int nrToMinGroupSize = minGroupSize - remainder; //how much extra students needed to create a min sized group with remainders
                if(nrGroupsWithMaxSize >= nrToMinGroupSize) //if there are more max sized groups than extra students needed, set is possible
                {
                    // solution is 544
                    nrGroupsWithMaxSize = nrGroupsWithMaxSize - nrToMinGroupSize; //take extra students from max sized groups
                    if(nrGroupsWithMaxSize != 0) //if there are still groups with max size
                    {
                        groupSizes.put(maxGroupSize, nrGroupsWithMaxSize); //create max sized groups
                    }
                    groupSizes.put(maxGroupSize - 1, nrToMinGroupSize); //create groups where extra students have been taken from
                    groupSizes.put(minGroupSize, 1); // create min sized group
                    matchingDone = true;
                }
                else
                {
                    maxGroupSize = maxGroupSize - 1; // Unable to create a set with current max group size, so reduce it
                }
            }
        }
        return groupSizes;
    }

}
