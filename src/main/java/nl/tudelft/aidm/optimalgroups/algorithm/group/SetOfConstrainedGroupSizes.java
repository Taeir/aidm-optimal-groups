package nl.tudelft.aidm.optimalgroups.algorithm.group;

import java.util.HashMap;
import java.util.Map;

final public class SetOfConstrainedGroupSizes
{
    /**
     * How will the algorithm create a set of group sizes with constraints?
     */
    enum SetCreationAlgorithm{
        MAX_FOCUS, // focuses on creating large groups first, then evenly reducing the size if necessary
        MIN_FOCUS // focuses on creating small groups first, then evenly increasing the size if necessary
    }

    public Map<Integer, Integer> setOfGroups; // A HashMap where the key is the size of a group and the value the amount of groups with that size
    private int nrStudents;
    private int minGroupSize;
    private int maxGroupSize;
    private SetCreationAlgorithm algorithm;

    /**
     * Given an amount of students and group size constraints, create a set of possible group sizes
     * This set can be used for assignment/allocation algorithms by keeping track how many of which group size can still be formed
     * To keep track of intermediate group sets, recordGroupFormedOfSize will decrement the amount of groups formable for that size, if possible
     * To check if a group with a certain size can be made, use mayFormGroupOfSize
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
    public boolean mayFormGroupOfSize(int groupSize){
        return (setOfGroups.containsKey(groupSize) && setOfGroups.get(groupSize) > 0);
    }

    /**
     * Try to form a group, decrementing the amount of that size if it succeeds
     * @param groupSize The size of a group
     */
    public void recordGroupFormedOfSize(int groupSize){
        if(!mayFormGroupOfSize(groupSize)){
            if(!setOfGroups.containsKey(groupSize)){
                throw new RuntimeException("Unable to create group with size " + groupSize + ", group size is not possible");

            }
            else{
                throw new RuntimeException("Unable to create group with size " + groupSize + ", maximum groups of that size reached");
            }
        }

        setOfGroups.put(groupSize, setOfGroups.get(groupSize) - 1);
    }

    /**
     * Calculate a possible set of group sizes
     */
    @SuppressWarnings("Duplicates")
    private void createSetOfGroups()
    {
        if(algorithm == SetCreationAlgorithm.MAX_FOCUS)
        {

            setOfGroups = new HashMap<>(); //Key = size of group, Value = amount of groups with that size
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
                    throw new RuntimeException("Impossible group matching problem, can't create a set of group sizes with current amount of students, group constraints and chosen set creation algorithm");
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
        else if(algorithm == SetCreationAlgorithm.MIN_FOCUS)
        {
            setOfGroups = new HashMap<>(); //Key = size of group, Value = amount of groups with that size
            boolean matchingDone = false;
            while(!matchingDone)
            {
                int nrGroupsWithMinSize = nrStudents / minGroupSize; //Create as many groups of min size as possible
                int remainder = nrStudents % minGroupSize; // Remaining students after creating as much groups with min size
                if(remainder == 0) //if no remaining students
                {
                    setOfGroups.put(minGroupSize, nrGroupsWithMinSize); //students are perfect fit for max group size
                    matchingDone = true;
                }
                else if(minGroupSize == maxGroupSize) //if there are remainders with a solo size constraint, impossible matching
                {
                    throw new RuntimeException("Impossible group matching problem, can't create a set of group sizes with current amount of students, group constraints and chosen set creation algorithm");
                }
                else //check if remainder can be added to other groups, without breaking group size constraints
                {
                    if(nrGroupsWithMinSize >= remainder) //if there are more min sized groups than remaining students, set is possible
                    {
                        nrGroupsWithMinSize = nrGroupsWithMinSize - remainder; //add remainder to min sized groups, so there are less groups with min size
                        if(nrGroupsWithMinSize != 0) //if there are still groups with min size
                        {
                            setOfGroups.put(minGroupSize, nrGroupsWithMinSize); //create min sized groups
                        }
                        setOfGroups.put(minGroupSize+1,remainder); //create groups where the remainder students have been put
                        matchingDone = true;
                    }
                    else
                    {
                        minGroupSize = minGroupSize- 1; // Unable to create a set with current min group size, so reduce it
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
