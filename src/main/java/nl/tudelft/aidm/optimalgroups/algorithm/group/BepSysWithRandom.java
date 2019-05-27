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

    public void constructGroupsFromCliques() {
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
            if (this.unavailableStudents.containsKey(pair.getKey())) {
                continue;
            } 
            
            if (equalFriendsLists(pair.getValue()) == false) {
                continue;
            }

            int[] friends = pair.getValue().groupPreference.asArray();
            List<Agent> friendsList = new ArrayList<Agent>();
            friendsList.add(pair.getValue());
            for (int friend : friends) {
                String friendString = String.valueOf(friend);
                Agent friendObj = this.availableStudents.remove(friendString);
                this.unavailableStudents.put(friendString, friendObj);
                
            }
            this.groups.add(friendsList);
            Agent self = this.availableStudents.remove(friend);
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
}
