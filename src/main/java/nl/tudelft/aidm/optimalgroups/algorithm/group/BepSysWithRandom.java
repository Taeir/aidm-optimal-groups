package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Agents;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Groups;
import nl.tudelft.aidm.optimalgroups.model.pref.AverageProjectPreferenceOfAgents;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.*;
import java.util.stream.Collectors;

public class BepSysWithRandom
{
    Agents students;

    Map<String, Agent> availableStudents;
    Map<String, Agent> unavailableStudents;

    int maxGroupSize;
    int minGroupSize;

    Groups groups;

    // Pass the list of students to make groups from
    public BepSysWithRandom(Agents students, int maxGroupSize, int minGroupSize) {
        this.students = students;

        this.availableStudents = new HashMap<String, Agent>();
        this.unavailableStudents = new HashMap<String, Agent>();

        this.maxGroupSize = maxGroupSize;
        this.minGroupSize = minGroupSize;

        for (Agent a : students.asCollection()) {
            this.availableStudents.put(a.name, a);
        }

        this.groups = new Groups();
    }

    public void constructGroups() {
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

        this.constructGroupsFromCliques();
        this.bestMatchUngrouped();
        this.mergeGroups();
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

        for (Map.Entry<String, Agent> pair : this.availableStudents.entrySet())
        {
            if (this.unavailableStudents.containsKey(pair.getKey()))
            {
                continue;
            }

            Agent student = pair.getValue();
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

                for (Agent studentInGroup : tentativeGroup.members().asCollection())
                {
                    this.availableStudents.remove(studentInGroup.name);
                    this.unavailableStudents.put(studentInGroup.name, studentInGroup);
                }
            }

        }
    }

    private void bestMatchUngrouped()
    {
        List<PossibleGroup> possibleGroups = new ArrayList<PossibleGroup>();

        for (Map.Entry<String, Agent> pair : this.availableStudents.entrySet()) {
            if (this.unavailableStudents.containsKey(pair.getKey())) continue; 
            
            List<Agent> friends = this.getAvailableFriends(pair.getValue());
            int score = this.computeScore(friends, pair.getValue());
            friends.add(pair.getValue()); // Add self to group
            possibleGroups.add(new PossibleGroup(friends, score));
        }

        this.pickBestGroups(possibleGroups);
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
            Group formedGroup = groups.addAsFormed(bestGroup.toGroup());

            for (Agent a : formedGroup.members().asCollection()) {
                this.availableStudents.remove(a.name);
                this.unavailableStudents.put(a.name, a);
            }
        }
    }

    private PossibleGroup findBestGroup(List<PossibleGroup> possibleGroups) {
        PossibleGroup bestGroup = possibleGroups.get(0);
        boolean available = false;
        while (possibleGroups != null && !available) {
            available = true;  
            for (Agent a : bestGroup.members) {
                if (this.unavailableStudents.containsKey(a.name)) {
                    available = false;
                    possibleGroups.remove(0);
                    bestGroup = possibleGroups.get(0);
                } 
            }
        }
        return bestGroup;
    }

    private void mergeGroups() {
        List<Group> merged = new ArrayList<>();
        List<Group.TentativeGroup> unmerged = new ArrayList<>();

        // todo: split groups into those that are finalized and those that are not
        //  s.t. no need to do this kind of case distinction here...
        for (Group g : this.groups.asCollection()) {
            if (g.members().count() == this.maxGroupSize) {
                merged.add(g);
            } else {
                unmerged.add(new Group.TentativeGroup(g));
            }
        }

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
                if (together == this.maxGroupSize &&  merged.size() < groupsMax) {
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
                    merged.add(unmergedGroup);
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

            // take best scoring group (it's a priority queue)
            PossibleGroupMerge bestMerge = possibleGroupMerges.peek();
            // remove the "other" group from unmerged
            unmerged.remove(bestMerge.g2); // todo: proper check for no candidates & exception

            Group.TentativeGroup tentativeGroup = bestMerge.toGroup();

            if (tentativeGroup.members().count() < this.maxGroupSize) {
                unmerged.add(tentativeGroup);
            }
            else if (tentativeGroup.members().count() == this.maxGroupSize) {
                Group theFormedGroup = groups.addAsFormed(tentativeGroup);
                merged.add(theFormedGroup); // todo: remove and or rename 'groups'
            }
            else {
                throw new RuntimeException("Group size is somehow larger than maximum group size");
            }
        }
    }


    private List<Agent> getAvailableFriends(Agent a) {
        List<Agent> friends = new ArrayList<Agent>();
        for (int i : a.groupPreference.asArray()) {
            if (this.availableStudents.containsKey(String.valueOf(i))) {
                friends.add(this.availableStudents.get(String.valueOf(i)));
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
