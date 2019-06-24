package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.entity.*;
import nl.tudelft.aidm.optimalgroups.model.pref.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class SOSM implements GroupFormingAlgorithm
{
    private Agents students;

    private Map<String, Agent> availableStudents;
    private Map<String, Agent> unavailableStudents;

    private final GroupSizeConstraint groupSizeConstraint;

    private FormedGroups tentativelyFormedGroups;
    private FormedGroups finalFormedGroups;

    private boolean done = false;

    private int studentsToAssign;

    // alternate bepsys with SOSM:
    // 1) create cliques first
    // 2) check if there are enough groupslots to fit all other students into. If yes, SOSM. Done
    // 3) create groups together based on peer preference
    // 4) check if there are enough "schools" to fit all other students into. If yes, SOSM. Done
    // 5) If not, randomly take students to form groups of 1 until there are enough groups to do SOSM
    // 6) Do SOSM. Done


    public SOSM(Agents students, GroupSizeConstraint groupSizeConstraint) {
        this.students = students;

        this.availableStudents = new HashMap<>();
        this.unavailableStudents = new HashMap<>();

        this.groupSizeConstraint = groupSizeConstraint;

        System.out.println("Student amount: " + students.count());

        for (Agent a : students.asCollection()) {
            this.availableStudents.put(a.name, a);
        }

        tentativelyFormedGroups = new FormedGroups();
        finalFormedGroups = new FormedGroups();

        this.studentsToAssign = students.count();
    }

    private void constructGroups() {

        //to make sure we can do SOSM, we need to know how many groups we can make minimum and maximum,
        //because we can't ensure SOSM can be applied otherwise,
        //since SOSM there are single students on one side and on the other side groupslots
        SetOfConstrainedGroupSizes maxGroupConstraints = new SetOfConstrainedGroupSizes(studentsToAssign, groupSizeConstraint, SetOfConstrainedGroupSizes.SetCreationAlgorithm.MIN_FOCUS);
        SetOfConstrainedGroupSizes minGroupConstraints = new SetOfConstrainedGroupSizes(studentsToAssign, groupSizeConstraint, SetOfConstrainedGroupSizes.SetCreationAlgorithm.MAX_FOCUS);
        int maxAmountOfGroups = 0;
        for(int i : maxGroupConstraints.setOfGroups.values()){
            maxAmountOfGroups += i;
        }
        int minAmountOfGroups = 0;
        for(int i : minGroupConstraints.setOfGroups.values()){
            minAmountOfGroups += i;
        }

        // now that we have a constraint on the amount of groups, we will use it.
        System.out.println(System.currentTimeMillis() + ": Start constructing cliques in SOSM");
        constructGroupsFromCliques();
        // if after cliques there are too few groups, just continue
        // if after cliques there are too many groups, impossible

        if(enoughGroupSlotsToAssignAllStudents())
        {
            doSOSM();
            this.done = true;
        }
        else
        {
            // first check if SOSM can be done
            System.out.println(System.currentTimeMillis() + ": Start matching ungrouped students in SOSM");
            bestMatchUngrouped();
            // first, after groupmatching, check if enough slots


            if(enoughGroupSlotsToAssignAllStudents())
            {
                doSOSM();
                this.done = true;
            }

            // if after groupmatching there are too few groups, form group of 1 until the minimum amount of groups have been reached
            if(tentativelyFormedGroups.count() < minAmountOfGroups)
            {
                while(tentativelyFormedGroups.count() < minAmountOfGroups)
                {
                    Agent randomStudent = this.students.asCollection().iterator().next();
                    Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(Agents.from(randomStudent), randomStudent.projectPreference);
                    this.tentativelyFormedGroups.addAsFormed(tentativeGroup);
                    this.availableStudents.remove(randomStudent.name);
                    this.unavailableStudents.put(randomStudent.name, randomStudent);
                }
                doSOSM();
                this.done = true;
            }
            // if after groupmatching there are too many groups, break groups until enough groups necessary
            else if(tentativelyFormedGroups.count() > maxAmountOfGroups)
            {
                while(tentativelyFormedGroups.count() > maxAmountOfGroups)
                {
                    // break a TentativelyFormedGroup
                    Group randomGroup = this.tentativelyFormedGroups.asCollection().iterator().next(); //remove random group
                    randomGroup.members().forEach(member -> {
                        this.unavailableStudents.remove(member.name);
                        this.availableStudents.put(member.name, member);
                    });
                    this.tentativelyFormedGroups = tentativelyFormedGroups.without(randomGroup);
                }
                if(enoughGroupSlotsToAssignAllStudents())
                {
                    doSOSM();
                    this.done = true;
                }
                else
                {
                    throw new RuntimeException("Incomplete case");
                }
            }
            else
            {
                throw new RuntimeException("SOSM is unable to properly divide a collection of groups and unformed students");
            }

        }

    }

    private void doSOSM(){


        //step 1:
        //each student proposes to first choice: choice is based on average project preference of group and student

        List<Group.TentativeGroup> unmerged = new ArrayList<>();

        // todo: split groups into those that are finalized and those that are not
        //  s.t. no need to do this kind of case distinction here...
        tentativelyFormedGroups.forEach(group -> {
            boolean groupIsOfMaximumSize = group.members().count() == this.groupSizeConstraint.maxSize();

            if (groupIsOfMaximumSize) {
                finalFormedGroups.addAsFormed(group);
            }
            else {
                unmerged.add(new Group.TentativeGroup(group));
            }
        });

        for(var group : groups) {
            var hyptheticalGroup = group.withExtraMember(student)
            var hypotheticalAggregGroupPref = hyptheticalGroup.aggregatedProjectPreference

            var peerSatisfaction = new PeerSatisfactionMetric(student, hyptheticalGroup)

            var score = peerSatisfaction.asPercentFloat() * new AUPCR.Student(student, hypotheticalAggregGroupPref) // don't rememeber the signature

                    ????
        }

        //we need to calculate for each student its priority for each group and vice-versa for groups.
        //only then can we assign them based on their priority order, which is what SOSM does.
        Map<String, Map<Integer, Integer>> studentGroupPreference = new HashMap<>(unmerged.size()); // student preference of certain group
        Map<String, Integer> groupStudentPreference = new HashMap<>(unmerged.size()); // group preference of certain student

        // per student
        this.availableStudents.forEach(student -> {
            // check per group what the best project is
            unmerged.forEach(group -> {


            });
        });
        for (Agent student : this.students.asCollection())
        {


            if (this.unavailableStudents.containsKey(student.name))
            {
                continue;
            }

        }

        Group.TentativeGroup unmergedGroup = unmerged.get(0);
        unmerged.remove(0);

        int unmergedGroupSize = unmergedGroup.members().count();

        var possibleGroupMerges = new PriorityQueue<>(Comparator.comparing(BepSysWithRandomGroups.PossibleGroupMerge::matchScore));

        for (Group.TentativeGroup otherUnmergedGroup : unmerged) {
            int together = unmergedGroupSize + otherUnmergedGroup.members().count();

            // Only add group if it is a final form
            if(groupConstraints.mayFormGroupOfSize(together)){
                possibleGroupMerges.add(new BepSysWithRandomGroups.PossibleGroupMerge(unmergedGroup, otherUnmergedGroup));
            }
        }

        System.out.println(System.currentTimeMillis() + ":\t\t- bestMatchUngrouped: " + this.availableStudents.size() + " students left to group");
        List<PossibleGroup> possibleGroups = new ArrayList<>();

        // Iterate over students instead of available students to prevent ConcurrentModificationException
        // when removing from availableStudents
        for (Agent student : this.students.asCollection()) {
            if (this.unavailableStudents.containsKey(student.name)) continue;

            List<Agent> friends = this.getAvailableFriends(student);
            int score = this.computeScore(friends, student);
            friends.add(student); // Add self to group
            possibleGroups.add(new PossibleGroup(friends, score));
        }

        this.pickBestGroups(possibleGroups);

        //each group tentatively assigns slots to one at a time following their priority order
        //any remaining proposers are rejected


    }

    private void constructGroupsFromCliques() {
        // For debugging purposes: keep amount of students in a clique
        int studentsInClique = 0;
        for (Agent student : this.students.asCollection())
        {
            if (this.unavailableStudents.containsKey(student.name))
            {
                continue;
            }

            if (students.hasEqualFriendLists(student))
            {
                int[] friends = student.groupPreference.asArray();
                List<Agent> clique = Arrays.stream(friends).mapToObj(String::valueOf)
                        .map(name -> students.findByAgentId(name))
                        .filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.toList());

                // also add the student
                clique.add(student);

                Agents agents = Agents.from(clique);

                Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(agents, ProjectPreferenceOfAgents.getChosenMethod(agents));
                System.out.println(System.currentTimeMillis() + ":\t\t- constructGroupsFromCliques: Clique formed of size " + clique.size());
                studentsInClique += clique.size();


                // Need to remove all members that have been formed into a group, from the 'availableStudents' map
                // however, doing so in a for(entry: map.entrySet() { ... } loop results in a concurrent modification exception
                // and because we have more than 1 student we need to remove from the map, we also cannot use iterators with iterator.remove()
                this.tentativelyFormedGroups.addAsFormed(tentativeGroup);
                for (Agent studentInGroup : tentativeGroup.members().asCollection())
                {
                    Agent removedAgent = this.availableStudents.remove(studentInGroup.name); // todo: more efficient remove (does keySet.removeall have the same semantics?)
                    this.unavailableStudents.put(studentInGroup.name, studentInGroup);
                }
            }
        }

        System.out.println(System.currentTimeMillis() + ":\t\t- constructGroupsFromCliques: Total students in cliques: " + studentsInClique + " of a total of " + this.students.count() + " students");
    }

    private void bestMatchUngrouped()
    {
        System.out.println(System.currentTimeMillis() + ":\t\t- bestMatchUngrouped: " + this.availableStudents.size() + " students left to group");
        List<PossibleGroup> possibleGroups = new ArrayList<>();

        // Iterate over students instead of available students to prevent ConcurrentModificationException
        // when removing from availableStudents
        for (Agent student : this.students.asCollection()) {
            if (this.unavailableStudents.containsKey(student.name)) continue;

            List<Agent> friends = this.getAvailableFriends(student);
            int score = this.computeScore(friends, student);
            friends.add(student); // Add self to group
            possibleGroups.add(new PossibleGroup(friends, score));
        }

        this.pickBestGroups(possibleGroups);
        System.out.println(System.currentTimeMillis() + ":\t\t- bestMatchUngrouped: done, " + this.availableStudents.size() + " students left to group");
    }

    public FormedGroups finalFormedGroups()
    {
        if (!done) {
            constructGroups();
        }

        return this.finalFormedGroups;
    }

    private void pickBestGroups(List<PossibleGroup> possibleGroups)
    {
        possibleGroups.sort(Comparator.comparing(o -> o.score));

        while (possibleGroups.size() > 0) {
            PossibleGroup bestGroup = findBestGroup(possibleGroups);
            if (bestGroup == null) continue; // todo: inf loop?

            Agents agents = Agents.from(bestGroup.members);
            Group formedGroup = tentativelyFormedGroups.addAsFormed(bestGroup.toGroup());

            for (Agent a : formedGroup.members().asCollection()) {
                this.availableStudents.remove(a.name);
                this.unavailableStudents.put(a.name, a);
            }
        }
    }

    private PossibleGroup findBestGroup(List<PossibleGroup> possibleGroups) {
        PossibleGroup possiblyBestGroup = null;
        boolean available = false;

        Iterator<PossibleGroup> bestGroupsIter = possibleGroups.iterator();
        while (bestGroupsIter.hasNext())
        {
            possiblyBestGroup = bestGroupsIter.next();
            available = true;

            // check if any of the members are already grouped
            for (Agent member : possiblyBestGroup.members)
            {
                if (unavailableStudents.containsKey(member.name)) {
                    available = false;
                    bestGroupsIter.remove();
                    break;
                }
            }
        }

        // none of the groups had a member that was available, hence no group found
        if (!available) {
            return null;
        }

        return possiblyBestGroup;
    }

    private void SOSM() {

    }

    public boolean enoughGroupSlotsToAssignAllStudents()
    {
        // Check if there are enough groupslots to fit all other students into
        int maxGroupSlots = 0;
        int minGroupSlots = 0;

        for (Group.FormedGroup group : this.tentativelyFormedGroups.asCollection()) {
            maxGroupSlots += Math.max(groupSizeConstraint.maxSize() - group.members().count(), 0);
            minGroupSlots += Math.max(groupSizeConstraint.minSize() - group.members().count(), 0);
        }

        studentsToAssign = students.count() - tentativelyFormedGroups.countTotalStudents();

        //If students are between boundaries, leftover students can be assigned to existing groups without breaking group size constraints
        System.out.println(minGroupSlots + " < " + studentsToAssign + " < " + maxGroupSlots);
        if(minGroupSlots <= studentsToAssign  && studentsToAssign <= maxGroupSlots)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public Collection<Group.FormedGroup> asCollection()
    {
        return finalFormedGroups().asCollection();
    }

    @Override
    public void forEach(Consumer<Group.FormedGroup> fn)
    {
        finalFormedGroups().forEach(fn);
    }

    @Override
    public int count()
    {
        return finalFormedGroups().count();
    }

    private List<Agent> getAvailableFriends(Agent a) {
        List<Agent> friends = new ArrayList<Agent>();
        for (int i : a.groupPreference.asArray()) {
            var friendName = String.valueOf(i);
            if (availableStudents.containsKey(friendName)) {
                friends.add(availableStudents.get(friendName));
            }
        }
        return friends;
    }

    private class PossibleGroup {
        public List<Agent> members;
        public int score;

        public PossibleGroup(List<Agent> members, int score) {
            this.members = members;
            this.score = score;
        }

        public Group.TentativeGroup toGroup()
        {
            Agents agents = Agents.from(members);
            return new Group.TentativeGroup(agents, ProjectPreferenceOfAgents.getChosenMethod(agents));
        }
    }

    private int computeScore(List<Agent> friends, Agent a)
    {
        int score = 0;
        for (Agent friend : friends) {
            List<Agent> friendsOfFriend = getAvailableFriends(friend);
            if (friendsOfFriend.contains(a)) score += 1;
            for (Agent friend2 : friends) {
                if (friend == friend2) continue;
                if (friendsOfFriend.contains(friend2)) score += 1;
            }
        }
        return score;
    }

}
