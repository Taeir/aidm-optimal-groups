package nl.tudelft.aidm.optimalgroups.algorithm.group;

import java.util.HashMap;
import java.util.Map;

final public class SetOfConstrainedGroupSizes
{
    /**
     * How will the algorithm create a set of group sizes with constraints?
     * MAX_FOCUS focuses on as many large groups as possible
     * MIN_FOCUS focuses on as many small groups as possible
     */
    enum SetCreationAlgorithm{
        MAX_FOCUS,
        MIN_FOCUS // Not implemented yet
    }

    public Map<Integer, Integer> setOfGroups; // A HashMap where the key is the size of a group and the value the amount of groups with that size
    private int nrStudents;
    private int minGroupSize;
    private int maxGroupSize;
    private SetCreationAlgorithm algorithm;

    /**
     * Given an amount of students and group size constraints, create a set of possible group sizes
     * This set can be used for assignment/allocation algorithms by keeping track how many of which group size can still be formed
     * To keep track of intermediate group sets, tryToFormGroup will decrement the amount of groups formable for that size, if possible
     * To check if a group with a certain size can be made, use groupSizePossible
     */
    public SetOfConstrainedGroupSizes(int nrStudents, int minGroupSize, int maxGroupSize, SetCreationAlgorithm algorithm){
        this.nrStudents = nrStudents;
        this.minGroupSize = minGroupSize;
        this.maxGroupSize = maxGroupSize;
        this.algorithm = algorithm;
        createSetOfGroups();
    }


    /**
     * Check if a group with a certain size can be formed
     * @param groupSize The size of the group
     * @return True if the group can be formed, false if not
     */
    public boolean groupSizePossible(int groupSize){
        return (setOfGroups.containsKey(groupSize));
    }

    /**
     * Try to form a group, decrementing the amount of that size if it succeeds
     * @param groupSize The size of a group
     * @return True if the group could be formed, false if not
     */
    public boolean tryToFormGroup(int groupSize){
        if(!groupSizePossible(groupSize)){
            return false;
        }
        int possibleGroups = setOfGroups.get(groupSize);
        if(possibleGroups == 1)
        {
            setOfGroups.remove(groupSize);
        }
        else
        {
            setOfGroups.put(groupSize, setOfGroups.get(groupSize) - 1);
        }
        return true;
    }

    /**
     * Calculate a possible set of group sizes
     * For now, the algorithm works from max size downwards, preferring to create groups with as many students as possible
     */
    private void createSetOfGroups()
    {
        if(algorithm == SetCreationAlgorithm.MAX_FOCUS)
        {

            setOfGroups = new HashMap<Integer, Integer>(); //Key = size of group, Value = amount of groups with that size
            boolean matchingDone = false;
            while(!matchingDone)
            {
                int nrGroupsWithMaxSize = nrStudents / maxGroupSize; //Create as many groups of max size as possible
                int remainder = nrStudents % maxGroupSize; // Remaining students after creating as much groups with max size
                if(remainder == 0) //if no remaining students
                {
                    setOfGroups.put(maxGroupSize, nrGroupsWithMaxSize); //students are perfect fit for max group size
                    matchingDone = true;
                }
                else if(minGroupSize == maxGroupSize) //if there are remainders with a solo size constraint, impossible matching
                {
                    throw new RuntimeException("Impossible group matching problem, can't create a set of group sizes with current amount of students and group constraints");
                }
                else if(remainder >= minGroupSize) //check if remainder fits in group size constraints
                {
                    setOfGroups.put(maxGroupSize, nrGroupsWithMaxSize); //create as much groups with max size possible
                    setOfGroups.put(remainder, 1); //then create a group with size of remainders
                    matchingDone = true;
                }
                else //check if remainder can steal enough from other groups to match size constraints
                {
                    int nrToMinGroupSize = minGroupSize - remainder; //how much extra students needed to create a min sized group with remainders
                    if(nrGroupsWithMaxSize >= nrToMinGroupSize) //if there are more max sized groups than extra students needed, set is possible
                    {
                        nrGroupsWithMaxSize = nrGroupsWithMaxSize - nrToMinGroupSize; //take 1 student from as many max sized groups as needed to reach minimum group size
                        if(nrGroupsWithMaxSize != 0) //if there are still groups with max size
                        {
                            setOfGroups.put(maxGroupSize, nrGroupsWithMaxSize); //create max sized groups
                        }
                        if(maxGroupSize - 1 == minGroupSize) // If groupsizes are 1 apart, add them together
                        {
                            setOfGroups.put(minGroupSize, nrToMinGroupSize + 1);
                        }
                        else
                        {
                            setOfGroups.put(maxGroupSize - 1, nrToMinGroupSize); //create groups where extra students have been taken from
                            setOfGroups.put(minGroupSize, 1); // create one min sized group
                        }
                        matchingDone = true;
                    }
                    else
                    {
                        maxGroupSize = maxGroupSize - 1; // Unable to create a set with current max group size, so reduce it
                    }
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("Wrong group set algorithm given");
        }
    }

}
