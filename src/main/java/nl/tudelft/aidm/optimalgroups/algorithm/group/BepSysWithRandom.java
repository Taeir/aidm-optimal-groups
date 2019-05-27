package nl.tudelft.aidm.optimalgroups.algorithm.group;

public class BepSysWithRandom
{
    Map<String, Agent> students;
    Map<String, Agent> availableStudents;
    Map<String, Agent> unavailableStudents;
    List<Group> groups;

    // Pass the list of students to make groups from
    public BepSysWithRandom(List<Agent> availableStudents) {
        this.availableStudents = new HashMap<String, Agent>();
        this.unavailableStudents = new HashMap<String, Agent>();

        for (Agent a : availableStudents) {
            this.availableStudents.put(a.name, a);
        }
        this.groups = new ArrayList<Group>;
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
            this.groups.add(new Group(0, Agents.from(friendsList), null)); //TO-DO: add sensible group id here
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
            int score = this.computeScore(friends, agent);
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
            int compare(PossibleGroup left, PossibleGroup right)  {
                return left.score - right.score;
            }
        });
        
        while (possibleGroups.size() > 0) {
            PossibleGroup bestGroup = findBestGroup(possibleGroups);
            if (bestGroup == null) continue;
            this.groups.add(new Group(0, Agents.from(bestGroup.members), null)); // TO-DO: add sensible group id here
            for (Agent a : bestGroup.members) {
                this.availableStudents.remove(a.name);
                this.unavailableStudents.add(a.name, a);
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
        // TO-DO
    }

    private List<Agent> getAvailableFriends(Agent a) {
        List<Agent> friends = new ArrayList<Agent>();
        for (int i : pair.getValue().groupPreference.asArray()) {
            if (this.availableStudents.containsKey(String.valueOf(i))) {
                friends.add(this.availableStudents.get(i));
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
