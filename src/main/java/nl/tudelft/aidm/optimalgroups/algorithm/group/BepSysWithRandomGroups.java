package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.entity.*;
import nl.tudelft.aidm.optimalgroups.model.pref.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BepSysWithRandomGroups implements GroupFormingAlgorithm
{
    private Agents students;

    private Map<String, Agent> availableStudents;
    private Map<String, Agent> unavailableStudents;

    private int maxGroupSize;
    private int minGroupSize;

    private FormedGroups tentativelyFormedGroups;
    private FormedGroups finalFormedGroups;

    private boolean done = false;

    // Pass the list of students to make groups from
    public BepSysWithRandomGroups(Agents students, int minGroupSize, int maxGroupSize) {
        this.students = students;

        this.availableStudents = new HashMap<>();
        this.unavailableStudents = new HashMap<>();

        this.minGroupSize = minGroupSize;
        this.maxGroupSize = maxGroupSize;

        System.out.println("Student amount: " + students.count());

        for (Agent a : students.asCollection()) {
            this.availableStudents.put(a.name, a);
        }

        tentativelyFormedGroups = new FormedGroups();
        finalFormedGroups = new FormedGroups();
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

        System.out.println(System.currentTimeMillis() + ": Start constructing cliques (state 1/3)");
        constructGroupsFromCliques();
        System.out.println(System.currentTimeMillis() + ": Start matching ungrouped students (state 2/3)");
        bestMatchUngrouped();

        // Check if all students are in groups and that there are no students with multiple groups (sanity check)
        Map<String, Integer> agentsInTentativelyFormedGroups = new HashMap<>();
        this.tentativelyFormedGroups.forEach(group -> {
            for (Agent a : group.members().asCollection()) {
                if (agentsInTentativelyFormedGroups.containsKey(a.name)) {
                    agentsInTentativelyFormedGroups.put(a.name, agentsInTentativelyFormedGroups.get(a.name) + 1);
                } else {
                    agentsInTentativelyFormedGroups.put(a.name, 1);
                }
            }
        });

        int cumulativeTentativeGroupSize = 0;
        for (Map.Entry<String, Integer> entry : agentsInTentativelyFormedGroups.entrySet()) {
            cumulativeTentativeGroupSize += entry.getValue();
        }

        System.out.println(System.currentTimeMillis() + ": Amount of students in multiple tentatively formed groups (should be zero!): " + (cumulativeTentativeGroupSize - agentsInTentativelyFormedGroups.size()));

        System.out.println(System.currentTimeMillis() + ": Start merging groups (state 3/3)");
        mergeGroups();
        System.out.println(System.currentTimeMillis() + ": Done!");

        // Check if all students are in groups and that there are no students with multiple groups (sanity check)
        Map<String, Integer> agentsInFinalGroups = new HashMap<>();
        this.finalFormedGroups.forEach(group -> {
            for (Agent a : group.members().asCollection()) {
                if (agentsInFinalGroups.containsKey(a.name)) {
                    agentsInFinalGroups.put(a.name, agentsInFinalGroups.get(a.name) + 1);
                } else {
                    agentsInFinalGroups.put(a.name, 1);
                }
            }
        });


        int cumulativeGroupSize = 0;
        for (Map.Entry<String, Integer> entry : agentsInFinalGroups.entrySet()) {
            cumulativeGroupSize += entry.getValue();
        }

        System.out.println(System.currentTimeMillis() + ": Amount of final groups: " + this.finalFormedGroups.count());
        System.out.println(System.currentTimeMillis() + ": Amount of students that are grouped: " + agentsInFinalGroups.size());
        System.out.println(System.currentTimeMillis() + ": Amount of students in multiple groups (should be zero!): " + (cumulativeGroupSize - agentsInFinalGroups.size()));

        this.done = true;
    }

    private Groups finalFormedGroups()
    {
        if (!done) {
            constructGroups();
        }

        return this.finalFormedGroups;
    }

    @Override
    public Collection<Group> asCollection()
    {
        return finalFormedGroups().asCollection();
    }

    @Override
    public void forEach(Consumer<Group> fn)
    {
        finalFormedGroups().forEach(fn);
    }

    @Override
    public int count()
    {
        return finalFormedGroups().count();
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

                Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(agents, new AverageProjectPreferenceOfAgents(agents));
                System.out.println(System.currentTimeMillis() + ": Clique formed of size " + clique.size());
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

        System.out.println(System.currentTimeMillis() + ": Total students in cliques: " + studentsInClique + " of a total of " + this.students.count() + " students");
    }

    private void bestMatchUngrouped()
    {
        System.out.println(System.currentTimeMillis() + ": bestMatchUngrouped: " + this.availableStudents.size() + " students left to group");
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
        System.out.println(System.currentTimeMillis() + ": bestMatchUngrouped: done, " + this.availableStudents.size() + " students left to group");
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

    private void mergeGroups() {
        List<Group.TentativeGroup> unmerged = new ArrayList<>();

        // todo: split groups into those that are finalized and those that are not
        //  s.t. no need to do this kind of case distinction here...
        tentativelyFormedGroups.forEach(group -> {
            boolean groupIsOfMaximumSize = group.members().count() == this.maxGroupSize;

            if (groupIsOfMaximumSize) {
                finalFormedGroups.addAsFormed(group);
            }
            else {
                unmerged.add(new Group.TentativeGroup(group));
            }
        });

        System.out.println(System.currentTimeMillis() + ": mergeGroups: " + finalFormedGroups.asCollection().size() + " max size (final) groups");
        System.out.println(System.currentTimeMillis() + ": mergeGroups: " + tentativelyFormedGroups.asCollection().size() + " groups to be merged");

        int numberOfStudents = this.students.count();
        int groupsMax = numberOfStudents / this.maxGroupSize;
        int remainder = numberOfStudents % this.maxGroupSize;
        int numberOfGroups = groupsMax + remainder / this.minGroupSize;
        remainder = remainder % this.minGroupSize;

        while (remainder > 0) {
            groupsMax -= 1;
            remainder += this.maxGroupSize;
            numberOfGroups += remainder / this.minGroupSize;
            remainder = remainder % this.minGroupSize;
        }

        System.out.println(System.currentTimeMillis() + ": mergeGroups: " + groupsMax + " groups of maximum size");
        System.out.println(System.currentTimeMillis() + ": mergeGroups: " + numberOfGroups + " groups in total");

        unmerged.sort(Comparator.comparingInt((Group group) -> group.members().count()));

        while (unmerged.size() > 0) {
            Group.TentativeGroup unmergedGroup = unmerged.get(0);
            unmerged.remove(0);

            int unmergedGroupSize = unmergedGroup.members().count();

            var possibleGroupMerges = new PriorityQueue<>(Comparator.comparing(PossibleGroupMerge::matchScore));

            for (Group.TentativeGroup otherUnmergedGroup : unmerged) {
                int together = unmergedGroupSize + otherUnmergedGroup.members().count();

                // Only keep scores if the size is equal to the maximum group size
                // Do we have a valid max-sizegroup and can we still create groups?
                if (together == this.maxGroupSize &&  this.finalFormedGroups.count() < groupsMax) {
                    possibleGroupMerges.add(new PossibleGroupMerge(unmergedGroup, otherUnmergedGroup));
                }
            }

            // if no candidate group merges
            if (possibleGroupMerges.size() == 0) {
                for (Group otherUnmergedGroup : unmerged) {
                    int together = unmergedGroupSize + otherUnmergedGroup.members().count();

                    // try again with relaxed constraints on group creation (up to group size)
                    if (together <= this.maxGroupSize) {
                        possibleGroupMerges.add(new PossibleGroupMerge(unmergedGroup, otherUnmergedGroup));
                    }
                }
            }

            // if still no candidate group merges
            if (possibleGroupMerges.size() == 0) {
                if (unmergedGroupSize >= this.minGroupSize) {
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
                    System.out.println(System.currentTimeMillis() + ": Nothing was removed from unmerged!!!");
                }

                Group.TentativeGroup tentativeGroup = bestMerge.toGroup();

                if (tentativeGroup.members().count() < this.maxGroupSize) {
                    unmerged.add(tentativeGroup);
                }
                else if (tentativeGroup.members().count() == this.maxGroupSize) {
                    finalFormedGroups.addAsFormed(tentativeGroup);
                }
                else {
                    throw new RuntimeException("Group size is somehow larger than maximum group size");
                }
            }
        }
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
            return new Group.TentativeGroup(agents, new AverageProjectPreferenceOfAgents(agents));
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
            ProjectPreference preferences = new AverageProjectPreferenceOfAgents(agents);

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

            int[] g1Prefs = g1.projectPreference().asArray();

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
