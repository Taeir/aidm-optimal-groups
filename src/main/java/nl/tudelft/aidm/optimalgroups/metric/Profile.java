package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.HashMap;
import java.util.Map;

public abstract class Profile {

    protected Matching<Group.FormedGroup, Project> matching;
    protected Map<Integer, Integer> profile = null;
    protected int maximumRank = 1;

    public Profile (Matching<Group.FormedGroup, Project> matching) {
        this.matching = matching;
    }

    public Map<Integer, Integer> asMap() {
        if (this.profile == null) {
            this.calculate();
            this.fillEmptyProfileValues();
        }
        return this.profile;
    }

    public int cumulativeRanks() {
        int result = 0;
        for (Map.Entry<Integer, Integer> entry : this.asMap().entrySet()) {
            int rank = entry.getKey();
            int quantity = entry.getValue();

            result += (rank * quantity);
        }

        return result;
    }

    abstract void calculate();

    public abstract void printResult();

    // Make sure all values are filled
    private void fillEmptyProfileValues() {
        for (int i = 1; i <= maximumRank; i++) {
            if (!this.profile.containsKey(i)) {
                this.profile.put(i, 0);
            }
        }
    }

    public static class StudentProjectProfile extends Profile {

        public StudentProjectProfile(Matching<Group.FormedGroup, Project> matching) {
            super(matching);
        }

        @Override
        void calculate() {
            this.profile = new HashMap<>();
            for (Match<Group.FormedGroup, Project> match : this.matching.asList()) {
                AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);

                assignedProjectRank.studentRanks().forEach(metric -> {
                    int studentsRank = metric.studentsRank();

                    // Student rank -1 indicates no project preference, hence we exclude
                    // in order to not inflate our performance
                    if (studentsRank == -1)
                        return;

                    this.maximumRank = Math.max(this.maximumRank, studentsRank);

                    if (this.profile.containsKey(studentsRank))
                        this.profile.put(studentsRank, this.profile.get(studentsRank) + 1);
                    else
                        this.profile.put(studentsRank, 1);
                });
            }
        }

        @Override
        public void printResult() {
            System.out.println("Student project profile results:");
            for (Map.Entry<Integer, Integer> entry : this.asMap().entrySet()) {
                System.out.printf("\t- Rank %d: %d student(s)\n", entry.getKey(), entry.getValue());
            }
            System.out.printf("\t- Cumulative rank of students: %d\n\n", this.cumulativeRanks());
        }
    }

    public static class GroupProjectProfile extends Profile {

        public GroupProjectProfile(Matching<Group.FormedGroup, Project> matching) {
            super(matching);
        }

        @Override
        void calculate() {
            this.profile = new HashMap<>();
            for (Match<Group.FormedGroup, Project> match : this.matching.asList()) {
                AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);
                int groupRank = assignedProjectRank.groupRank();

                this.maximumRank = Math.max(this.maximumRank, groupRank);

                if (this.profile.containsKey(groupRank))
                    this.profile.put(groupRank, this.profile.get(groupRank) + 1);
                else
                    this.profile.put(groupRank, 1);

            }
        }

        @Override
        public void printResult() {
            System.out.println("Group project profile results:");
            for (Map.Entry<Integer, Integer> entry : this.asMap().entrySet()) {
                System.out.printf("\t- Rank %d: %d group(s)\n", entry.getKey(), entry.getValue());
            }
            System.out.printf("\t- Cumulative rank of groups: %d\n\n", this.cumulativeRanks());
        }
    }
}
