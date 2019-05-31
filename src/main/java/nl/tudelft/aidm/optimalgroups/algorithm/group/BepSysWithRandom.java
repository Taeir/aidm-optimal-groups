package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Agents;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Groups;

import java.util.*;
import java.util.function.Function;

public class BepSysWithRandom
{
    Map<String, Agent> students;
    Map<String, Agent> availableStudents;
    Map<String, Agent> unavailableStudents;

    int maxGroupSize;
    int minGroupSize;

    Groups groups;

    // Pass the list of students to make groups from
    public BepSysWithRandom(List<Agent> availableStudents, int maxGroupSize, int minGroupSize) {
        this.availableStudents = new HashMap<String, Agent>();
        this.unavailableStudents = new HashMap<String, Agent>();
        this.maxGroupSize = maxGroupSize;
        this.minGroupSize = minGroupSize;

        for (Agent a : availableStudents) {
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

        for (Map.Entry<String, Agent> pair : this.students.entrySet()) {
            if (this.unavailableStudents.containsKey(pair.getKey())) continue;
            
            if (equalFriendsLists(pair.getValue()) == false) continue;

            int[] friends = pair.getValue().groupPreference.asArray();
            List<Agent> friendsList = new ArrayList<Agent>();
            friendsList.add(pair.getValue());
            for (int friend : friends) {
                String friendString = String.valueOf(friend);
                Agent friendObj = this.availableStudents.remove(friendString);
                this.unavailableStudents.put(friendString, friendObj);
                
            }
            groups.makeGroup(Agents.from(friendsList), null);
            Agent self = this.availableStudents.remove(pair.getKey());
            this.unavailableStudents.put(self.name, self);
        }
    }

    private boolean equalFriendsLists(Agent agent) {
        /* RUBY CODE:
        # get friends of student
        friends = StudentPreference.preferences_for(participant, @course_edition)
                                .map(&:student)
        friends.delete_if { |x| !@course_edition.approved_participants.include? x }

        base_friends = friends + [participant]
        equal = true
        friends.each do |friend|
            # for each friend, check if their friends lists are equal
            friends_of_friend =
                StudentPreference.preferences_for(friend, @course_edition)
                            .map(&:student) + [friend]
            equal = false unless friends_of_friend.sort == base_friends.sort
        end
        [equal, friends]

        */
        Set<String> friends = new HashSet<String>();
        friends.add(agent.name); //Add agent himself to set
        for (int i : agent.groupPreference.asArray()) {
            friends.add(String.valueOf(i));
        }
        for (String friend : friends) {
            Set<String> friendsOfFriends = new HashSet<String>();
            friendsOfFriends.add(friend); // Add friend himself to list
            for (int i : this.availableStudents.get(friend).groupPreference.asArray()) {
                friendsOfFriends.add(String.valueOf(i));
            }
            if (friends.equals(friendsOfFriends) == false) {
                return false;
            }
        }

        return true;
    }

    private void bestMatchUngrouped() {
        List<PossibleGroup> possibleGroups = new ArrayList<PossibleGroup>();

        for (Map.Entry<String, Agent> pair : this.students.entrySet()) {
            if (this.unavailableStudents.containsKey(pair.getKey())) continue; 
            
            List<Agent> friends = this.getAvailableFriends(pair.getValue());
            int score = this.computeScore(friends, pair.getValue());
            friends.add(pair.getValue()); // Add self to group
            possibleGroups.add(new PossibleGroup(friends, score));
        }
        this.pickBestGroups(possibleGroups);
    }

    private int computeScore(List<Agent> friends, Agent a) {
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

    private void pickBestGroups(List<PossibleGroup> possibleGroups) {
        Collections.sort(possibleGroups, new Comparator<PossibleGroup>() {
            public int compare(PossibleGroup left, PossibleGroup right)  {
                return right.score - left.score;
            }
        });
        
        while (possibleGroups.size() > 0) {
            PossibleGroup bestGroup = findBestGroup(possibleGroups);
            if (bestGroup == null) continue;
            this.groups.makeGroup(Agents.from(bestGroup.members), null); // TODO: preferences?
            for (Agent a : bestGroup.members) {
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
        List<Group> merged = new ArrayList<Group>();
        List<Group> unmerged = new ArrayList<Group>();
        for (Group g : this.groups.asCollection()) {
            if (g.members().count() == this.maxGroupSize) {
                merged.add(g);
            } else {
                unmerged.add(g);
            }
        }

        int count = merged.size();
        int numberOfStudents = this.students.size();
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

        Collections.sort(unmerged, new Comparator<Group>() {
            public int compare(Group left, Group right)  {
                return left.members().count() - right.members().count();
            }
        });

        while (unmerged.size() > 0) {
            Group unmergedGroup = unmerged.get(0);
            int unmergedGroupSize = unmergedGroup.members().count();
            unmerged.remove(0);
            Map<Integer, Group> scores = new HashMap<Integer, Group>();
            for (Group otherUnmergedGroup : unmerged) {
                int together = unmergedGroupSize + otherUnmergedGroup.members().count();

                // Only keep scores if the size is equal to the maximum group size
                if (together == this.maxGroupSize && count < groupsMax) {
                    scores.put(computePrefScore(unmergedGroup, otherUnmergedGroup), otherUnmergedGroup);
                }
            }
            if (scores.size() == 0) {
                for (Group otherUnmergedGroup : unmerged) {
                    int together = unmergedGroupSize + otherUnmergedGroup.members().count();

                    // Keep scores if it does not exceed the maximum group size
                    if (together <= this.maxGroupSize) {
                        scores.put(computePrefScore(unmergedGroup, otherUnmergedGroup), otherUnmergedGroup);
                    }
                }
            }
            if (scores.size() == 0) {
                if (unmergedGroupSize < this.minGroupSize) {
                    // Divide all people of this group
                    for (Agent a : unmergedGroup.members().asCollection()) {
                        Group newGroup = new Group(0, Agents.from(new Agent[]{a}), null); //TO-DO: add sensible id here
                        unmerged.add(newGroup);
                    }
                } else {
                    merged.add(unmergedGroup);
                    continue;
                }
            }

            // Get partner group
            int maxScore = Collections.max(scores.keySet());
            Group partnerGroup = scores.get(maxScore);
            unmerged.remove(partnerGroup);

            // Construct new merged group
            ArrayList<Agent> newMembers = new ArrayList<Agent>();
            newMembers.addAll(unmergedGroup.members().asCollection());
            newMembers.addAll(partnerGroup.members().asCollection());
            Group newGroup = new Group(0, Agents.from(newMembers.toArray(new Agent[newMembers.size()])), null); //TO-DO: add sensible id here
            
            int newGroupSize = newGroup.members().count();
            if (newGroupSize < this.maxGroupSize) {
                unmerged.add(newGroup);
            } else if (newGroupSize == this.maxGroupSize) {
                count += 1;
                merged.add(newGroup);
            } else {
                throw new RuntimeException("Group size is somehow larger than maximum group size");
            }
        }
        this.groups = merged;
    }

    private int computePrefScore(Group g1, Group g2) {
        /*
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
        int score = 0;
//        Map<Integer, Integer> g1Prefs = sortByValue(computeAveragePrefs(g1));
//
//        Map<Integer, Integer> g2Prefs = sortByValue(computeAveragePrefs(g2));
//
//        Set<Integer> g1Sorted = g1Prefs.keySet();
//        Set<Integer> g2Sorted = g2Prefs.keySet();

        int count = 0;

        int[] g1Prefs = g1.projectPreference().asArray();
        int[] g2Prefs = g2.projectPreference().asArray();

        // optimization & readability: finding rank of preference in g2's preferences
        HashMap<Integer, Integer> g2PrefMap = new HashMap<>(g2Prefs.length);
        for (int i = 0; i < g2Prefs.length; i++)
        {
            g2PrefMap.put(g2Prefs[i], i);
        }
        Function<Integer, Integer> findPreferenceRankInGroup2 = (pref) -> g2PrefMap.getOrDefault(pref, -1);

        for (int g1Pref : g1Prefs) {
            int count2 = findPreferenceRankInGroup2.apply(g1Pref);

            if (count2 >= 0) {
                score += (count - count2) * (count - count2);
            }

            count += 1;
        }

        return score;
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
    }
}
