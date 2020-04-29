package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.*;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.pref.*;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreferenceOfAgents;


import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BepSysImprovedGroups implements GroupFormingAlgorithm
{
    private Agents students;

    private HashSet<Agent> availableStudents;
    private HashSet<Agent> unavailableStudents;

    private final GroupSizeConstraint groupSizeConstraint;

    private FormedGroups tentativelyFormedGroups;
    private FormedGroups finalFormedGroups;

    private boolean useImprovedAlgo;

    private boolean done = false;

    // Pass the list of students to make groups from
    public BepSysImprovedGroups(Agents agents, GroupSizeConstraint groupSizeConstraint, boolean useImprovedAlgo) {
        this.students = agents;

        this.availableStudents = new HashSet<>();
        this.unavailableStudents = new HashSet<>();

        this.groupSizeConstraint = groupSizeConstraint;

//        System.out.println("Student amount: " + students.count());

        this.availableStudents.addAll(students.asCollection());

        tentativelyFormedGroups = new FormedGroups();
        finalFormedGroups = new FormedGroups();

        this.useImprovedAlgo = useImprovedAlgo;
    }

    private void constructGroups() {
        /*
        _, nogroup, group_list = clique_friends
        best_match = best_match_ungrouped nogroup
        best_match.each do |group|
            group_list.append group
        end

        group_list = merge_groups group_list

        db_list = []
        group_list.each do |group|
            participant = group[:leader]
            friends = group[:members]
            db_list.append create_group_without_project participant, friends
        end
        db_list
        */

//        System.out.println(System.currentTimeMillis() + ": Start constructing cliques (state 1/3)");
        constructGroupsFromCliques();
//        System.out.println(System.currentTimeMillis() + ": Start matchings ungrouped students (state 2/3)");
        bestMatchUngrouped();

        // Check if all students are in groups and that there are no students with multiple groups (sanity check)
        Map<String, Integer> agentsInTentativelyFormedGroups = new HashMap<>();
        this.tentativelyFormedGroups.forEach(group -> {
            for (Agent a : group.members().asCollection()) {
                var agentName = a.id.toString();
                if (agentsInTentativelyFormedGroups.containsKey(agentName)) {
                    agentsInTentativelyFormedGroups.put(agentName, agentsInTentativelyFormedGroups.get(agentName) + 1);
                } else {
                    agentsInTentativelyFormedGroups.put(agentName, 1);
                }
            }
        });

        int cumulativeTentativeGroupSize = 0;
        for (Map.Entry<String, Integer> entry : agentsInTentativelyFormedGroups.entrySet()) {
            cumulativeTentativeGroupSize += entry.getValue();
        }

//        System.out.println(System.currentTimeMillis() + ": Amount of students in multiple tentatively formed groups (should be zero!): " + (cumulativeTentativeGroupSize - agentsInTentativelyFormedGroups.size()));

//        System.out.println(System.currentTimeMillis() + ": Start merging groups (state 3/3)");
        mergeGroups();
//        System.out.println(System.currentTimeMillis() + ": Done!");

        // Check if all students are in groups and that there are no students with multiple groups (sanity check)
        Map<String, Integer> agentsInFinalGroups = new HashMap<>();
        this.finalFormedGroups.forEach(group -> {
            for (Agent a : group.members().asCollection()) {
                var agentName = a.id.toString();
                if (agentsInFinalGroups.containsKey(agentName)) {
                    agentsInFinalGroups.put(agentName, agentsInFinalGroups.get(agentName) + 1);
                } else {
                    agentsInFinalGroups.put(agentName, 1);
                }
            }
        });


        int cumulativeGroupSize = 0;
        for (Map.Entry<String, Integer> entry : agentsInFinalGroups.entrySet()) {
            cumulativeGroupSize += entry.getValue();
        }

//        System.out.println(System.currentTimeMillis() + ": Amount of final groups: " + this.finalFormedGroups.count());
//        System.out.println(System.currentTimeMillis() + ": Amount of students that are grouped: " + agentsInFinalGroups.size());
//        System.out.println(System.currentTimeMillis() + ": Amount of students in multiple groups (should be zero!): " + (cumulativeGroupSize - agentsInFinalGroups.size()));

        this.done = true;
    }

    @Override
    public FormedGroups asFormedGroups()
    {
        if (!done) {
            constructGroups();
        }

        return this.finalFormedGroups;
    }

    @Override
    public Collection<Group.FormedGroup> asCollection()
    {
        return asFormedGroups().asCollection();
    }

    @Override
    public void forEach(Consumer<Group.FormedGroup> fn)
    {
        asFormedGroups().forEach(fn);
    }

    @Override
    public int count()
    {
        return asFormedGroups().count();
    }

    private void constructGroupsFromCliques() {
        /* RUBY CODE:
        group_list = []
        no_group = []
        grouped = []
        @course_edition.approved_participants.each do |participant|
          next if grouped.include? participant
          equal, friends = equal_friends_lists participant
          # if they are not equal, add this student to the no_group list
          # and move on to the next
          no_group.append participant unless equal
          next unless equal
          # if they are equal, form a group
          group = { leader: participant, members: friends }
          group_list.append(group)
          grouped.append(participant)
          friends.each do |friend|
            grouped.append(friend)
          end
        end
        [grouped, no_group, group_list]
        */

        // For debugging purposes: keep amount of students in a clique
        int studentsInClique = 0;
        for (Agent student : this.students.asCollection())
        {
            if (this.unavailableStudents.contains(student))
            {
                continue;
            }

            if(useImprovedAlgo && student.groupPreferenceLength() > groupSizeConstraint.maxSize()) //Don't allow cliques larger than max group size with improved algo
            {
                continue;
            }

            if (students.hasEqualFriendLists(student))
            {
                int[] friends = student.groupPreference.asArray();
                List<Agent> clique = Arrays.stream(friends)
                    .mapToObj(name -> students.findByAgentId(name))
                    .filter(Optional::isPresent).map(Optional::get)
                    .collect(Collectors.toList());

                // also add the student
                clique.add(student);

                Agents agents = Agents.from(clique);

                Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(agents, ProjectPreferenceOfAgents.aggregateWithGloballyConfiguredAggregationMethod(agents));
//                System.out.println(System.currentTimeMillis() + ":\t\t- constructGroupsFromCliques: Clique formed of size " + clique.size());
                studentsInClique += clique.size();


                // Need to remove all members that have been formed into a group, from the 'availableStudents' map
                // however, doing so in a for(entry: map.entrySet() { ... } loop results in a concurrent modification exception
                // and because we have more than 1 student we need to remove from the map, we also cannot use iterators with iterator.remove()
                this.tentativelyFormedGroups.addAsFormed(tentativeGroup);
                for (Agent studentInGroup : tentativeGroup.members().asCollection())
                {
                    this.availableStudents.remove(studentInGroup); // todo: more efficient remove (does keySet.removeall have the same semantics?)
                    this.unavailableStudents.add(studentInGroup);
                }
            }
        }

//        System.out.println(System.currentTimeMillis() + ":\t\t- constructGroupsFromCliques: Total students in cliques: " + studentsInClique + " of a total of " + this.students.count() + " students");
    }

    private void bestMatchUngrouped()
    {
//        System.out.println(System.currentTimeMillis() + ":\t\t- bestMatchUngrouped: " + this.availableStudents.size() + " students left to group");
        List<PossibleGroup> possibleGroups = new ArrayList<>();

        // Iterate over students instead of available students to prevent ConcurrentModificationException
        // when removing from availableStudents
        for (Agent student : this.students.asCollection()) {
            if (this.unavailableStudents.contains(student)) continue;

            List<Agent> friends = this.getAvailableFriends(student);
            int score = this.computeScore(friends, student);
            friends.add(student); // Add self to group
            possibleGroups.add(new PossibleGroup(friends, score));
        }

        this.pickBestGroups(possibleGroups);
//        System.out.println(System.currentTimeMillis() + ":\t\t- bestMatchUngrouped: done, " + this.availableStudents.size() + " students left to group");
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

    private void pickBestGroups(List<PossibleGroup> possibleGroups)
    {
        possibleGroups.sort(Comparator.comparing(o -> o.score));

        while (possibleGroups.size() > 0) {
            PossibleGroup bestGroup = findBestGroup(possibleGroups);
            if (bestGroup == null) continue; // todo: inf loop?

            Agents agents = Agents.from(bestGroup.members);
            Group formedGroup = tentativelyFormedGroups.addAsFormed(bestGroup.toGroup());

            for (Agent a : formedGroup.members().asCollection()) {
                this.availableStudents.remove(a);
                this.unavailableStudents.add(a);
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
                if (unavailableStudents.contains(member)) {
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

    private void mergeGroups() {
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

//        System.out.println(System.currentTimeMillis() + ":\t\t- mergeGroups: " + finalFormedGroups.asCollection().size() + " max size (final) groups");
//        System.out.println(System.currentTimeMillis() + ":\t\t- mergeGroups: " + tentativelyFormedGroups.asCollection().size() + " groups to be merged");

        unmerged.sort(Comparator.comparingInt((Group group) -> group.members().count()));

        SetOfGroupSizesThatCanStillBeCreated groupConstraints = null;
        int groupsMax = 0;

        if(useImprovedAlgo)
        {
            int amountOfStudentsToGroup = tentativelyFormedGroups.countTotalStudents() - finalFormedGroups.countTotalStudents();
            groupConstraints = new SetOfGroupSizesThatCanStillBeCreated(amountOfStudentsToGroup, groupSizeConstraint, SetOfGroupSizesThatCanStillBeCreated.FocusMode.MAX_FOCUS);
        }
        else
        {
            int henk = groupSizeConstraint.minSize();
            int maxGroupSize = groupSizeConstraint.maxSize();
            int minGroupSize = groupSizeConstraint.minSize();

            int numberOfStudents;
            int remainder;
            int numberOfGroups;

            // hacky: outer while loop with a groupsMax == 0 check
            // the problem is that the original algorithm does not
            // consider the values between minGroupSize and maxGroupSize
            // and hence might end up in a inf loop / fail. Our band-aid fix
            // is to detect this (groupMax == 0) and increment the minGroupSize
            // and retry the mathematical algorithm. We do this in a temp var called
            // 'henk' to emphasize the hacky nature of the fix
            while (true) {
                numberOfStudents = this.students.count();
                groupsMax = numberOfStudents / maxGroupSize;
                remainder = numberOfStudents % maxGroupSize;
                numberOfGroups = groupsMax + remainder / minGroupSize;
                remainder = remainder % minGroupSize;

                while (remainder > 0) {
                    groupsMax -= 1;
                    remainder += maxGroupSize;
                    numberOfGroups += remainder / henk;
                    remainder = remainder % henk;

                    if (groupsMax == 0) {
                        henk++;
                        break;
                    }
                }

                if (remainder == 0)
                    break;
            }
//            System.out.println(System.currentTimeMillis() + ":\t\t- mergeGroups: " + groupsMax + " groups of maximum size");
//            System.out.println(System.currentTimeMillis() + ":\t\t- mergeGroups: " + numberOfGroups + " groups in total");
        }

        while (unmerged.size() > 0) {
            if(useImprovedAlgo)
            {
                Group.TentativeGroup unmergedGroup = unmerged.get(0);
                unmerged.remove(0);

                int unmergedGroupSize = unmergedGroup.members().count();

                var possibleGroupMerges = new PriorityQueue<>(Comparator.comparing(BepSysImprovedGroups.PossibleGroupMerge::matchScore));

                for (Group.TentativeGroup otherUnmergedGroup : unmerged) {
                    int together = unmergedGroupSize + otherUnmergedGroup.members().count();

                    // Only add group if it is a final form
                    if(groupConstraints.mayFormGroupOfSize(together)){
                        possibleGroupMerges.add(new BepSysImprovedGroups.PossibleGroupMerge(unmergedGroup, otherUnmergedGroup));
                    }
                }

                // if no candidate group merges
                if (possibleGroupMerges.size() == 0) {
                    for (Group otherUnmergedGroup : unmerged) {
                        int together = unmergedGroupSize + otherUnmergedGroup.members().count();

                        // try again with relaxed constraints on group creation (up to group size)
                        if (together <= groupSizeConstraint.maxSize()) {
                            possibleGroupMerges.add(new PossibleGroupMerge(unmergedGroup, otherUnmergedGroup));
                        }
                    }
                }

                // if still no candidate group merges
                if (possibleGroupMerges.size() == 0) {
                    for (Agent a : unmergedGroup.members().asCollection()) {
                        Agents singleAgent = Agents.from(a);
                        unmerged.add(new Group.TentativeGroup(singleAgent, a.projectPreference));
                    }
                }

                if (possibleGroupMerges.size() > 0)
                {
                    // take best scoring group (it's a priority queue)
                    PossibleGroupMerge bestMerge = possibleGroupMerges.peek();
                    // remove the "other" group from unmerged
                    boolean removedSomething = unmerged.remove(bestMerge.g2); // todo: proper check for no candidates & exception
                    if (!removedSomething) {
//                        System.out.println(System.currentTimeMillis() + ":\t\t- mergeGroups: Nothing was removed from unmerged!!!");
                    }

                    Group.TentativeGroup tentativeGroup = bestMerge.toGroup();
                    int together = tentativeGroup.members().count();

                    if(groupConstraints.mayFormGroupOfSize(together)) //Does the merged group fit in group size constraints?
                    {
                        groupConstraints.recordGroupFormedOfSize(together);
                        finalFormedGroups.addAsFormed(tentativeGroup);
                    }
                    else
                    {
                        unmerged.add(tentativeGroup); // If merge can't be in final set, at least keep it
                    }
                }
            }
            else
            {
                Group.TentativeGroup unmergedGroup = unmerged.get(0);
                unmerged.remove(0);

                int unmergedGroupSize = unmergedGroup.members().count();

                var possibleGroupMerges = new PriorityQueue<>(Comparator.comparing(PossibleGroupMerge::matchScore));

                for (Group.TentativeGroup otherUnmergedGroup : unmerged) {
                    int together = unmergedGroupSize + otherUnmergedGroup.members().count();

                    // Only keep scores if the size is equal to the maximum group size
                    // Do we have a valid max-sizegroup and can we still create groups?
                    if (together == groupSizeConstraint.maxSize() &&  this.finalFormedGroups.count() < groupsMax) {
                        possibleGroupMerges.add(new PossibleGroupMerge(unmergedGroup, otherUnmergedGroup));
                    }
                }

                // if no candidate group merges
                if (possibleGroupMerges.size() == 0) {
                    for (Group otherUnmergedGroup : unmerged) {
                        int together = unmergedGroupSize + otherUnmergedGroup.members().count();

                        // try again with relaxed constraints on group creation (up to group size)
                        if (together <= groupSizeConstraint.maxSize()) {
                            possibleGroupMerges.add(new PossibleGroupMerge(unmergedGroup, otherUnmergedGroup));
                        }
                    }
                }

                // if still no candidate group merges
                if (possibleGroupMerges.size() == 0) {
                    if (unmergedGroupSize >= groupSizeConstraint.minSize()) {
                        // satisfies min size constraint, accept the group
                        finalFormedGroups.addAsFormed(unmergedGroup);
                    }
                    // Group does not meet minimal group size: split and hope for best in next iter
                    else {
                        // Divide all people of this group
                        for (Agent a : unmergedGroup.members().asCollection()) {
                            Agents singleAgent = Agents.from(a);
                            unmerged.add(new Group.TentativeGroup(singleAgent, a.projectPreference));
                        }
                    }
                }

                if (possibleGroupMerges.size() > 0)
                {
                    // take best scoring group (it's a priority queue)
                    PossibleGroupMerge bestMerge = possibleGroupMerges.peek();
                    // remove the "other" group from unmerged
                    boolean removedSomething = unmerged.remove(bestMerge.g2); // todo: proper check for no candidates & exception
                    if (!removedSomething) {
//                        System.out.println(System.currentTimeMillis() + ":\t\t- mergeGroups: Nothing was removed from unmerged!!!");
                    }

                    Group.TentativeGroup tentativeGroup = bestMerge.toGroup();

                    if (tentativeGroup.members().count() < groupSizeConstraint.maxSize()) {
                        unmerged.add(tentativeGroup);
                    }
                    else if (tentativeGroup.members().count() == groupSizeConstraint.maxSize()) {
                        finalFormedGroups.addAsFormed(tentativeGroup);
                    }
                    else {
                        throw new RuntimeException("mergeGroups: Group size is somehow larger than maximum group size");
                    }
                }
            }
        }
    }


    private List<Agent> getAvailableFriends(Agent a) {
        List<Agent> friends = new ArrayList<>();

        // Some friends might be unavilable? If so, need to get group prefs and check
        for (var friend : a.groupPreference.asList()) {
            if (availableStudents.contains(friend)) {
                availableStudents.remove(friend);
                friends.add(friend);
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
            return new Group.TentativeGroup(agents, ProjectPreferenceOfAgents.aggregateWithGloballyConfiguredAggregationMethod(agents));
        }
    }

    private static class PossibleGroupMerge
    {
        Group g1;
        Group g2;

        public PossibleGroupMerge(Group g1, Group g2)
        {
            this.g1 = g1;
            this.g2 = g2;
        }

        private int score = -1;
        int matchScore()
        {
            if (score == -1) {
                score = computeMatchScore();
            }

            return score;
        }

        public Group.TentativeGroup toGroup()
        {
            Agents agents = g1.members().with(g2.members());
            ProjectPreference preferences = ProjectPreferenceOfAgents.aggregateWithGloballyConfiguredAggregationMethod(agents);

            return new Group.TentativeGroup(agents, preferences);
        }

        private int computeMatchScore() {
            /* RUBY CODE:
                score = 0
                g1users = g1[:members].each { |m| m } + [g1[:leader]]
                g2users = g2[:members].each { |m| m } + [g2[:leader]]

                g1prefs = comp_average_pref g1users
                g2prefs = comp_average_pref g2users

                g1sorted = g1prefs.sort_by { |_k, v| v }.map { |v| v[0] }
                g2sorted = g2prefs.sort_by { |_k, v| v }.map { |v| v[0] }

                cnt = 0
                g1sorted.each do |p|
                    cnt2 = g2sorted.index(p)
                    score += (cnt - cnt2) * (cnt - cnt2) unless cnt2.nil?
                    cnt += 1
                end

                score
            */

            Integer[] g1Prefs = g1.projectPreference().asArray();

            int score = 0;
            for (int prefRankG1 = 0; prefRankG1 < g1Prefs.length; prefRankG1++) {
                int prefRankG2OfG1sPref = findPreferenceRankInGroup2(g1Prefs[prefRankG1]);

                if (prefRankG2OfG1sPref >= 0) {
                    score += Math.pow(prefRankG1 - prefRankG2OfG1sPref, 2);
                }
            }

            return score;
        }

        private HashMap<Integer, Integer> g2PrefMap;
        private int findPreferenceRankInGroup2(int pref)
        {
            if (g2PrefMap == null) {
                // mapping not created yet, do so now
                var g2Prefs = g2.projectPreference().asArray();
                g2PrefMap = new HashMap<>(g2Prefs.length);
                for (int i = 0; i < g2Prefs.length; i++)
                {
                    g2PrefMap.put(g2Prefs[i], i);
                }
            }

            return g2PrefMap.getOrDefault(pref, -1);
        }
    }
}
