package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matchings;
import nl.tudelft.aidm.optimalgroups.model.*;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public abstract class AUPCR {

    protected Matchings<? extends Group, Project> matchings;
    protected Projects projects;
    protected Agents students;

    protected float aupcr = -1;

    /**
     * Class to implement the Area Under Profile Curve Ratio (AUPCR) metric.
     * An AUPCR of 1 is perfect and an AUPCR of 0 is terrible.
     * Defined on page 8 of (Diebold & Bichler, 2016)
     */
    public AUPCR (Matchings<? extends Group, Project> matchings, Projects projects, Agents students) {
        this.matchings = matchings;
        this.projects = projects;
        this.students = students;
    }

    public float result() {
        if (this.aupcr == -1) {
            this.aupcr = ((float) this.aupc()) / this.totalArea();
        }
        return this.aupcr;
    }

    public abstract void printResult();

    protected abstract float totalArea();

    protected abstract int aupc();

    public static class StudentAUPCR extends AUPCR {
        public StudentAUPCR (Matchings<? extends Group, Project> matchings, Projects projects, Agents students) {
            super(matchings, projects, students);
        }

        @Override
        public void printResult() {
            System.out.printf("Students AUPCR: %f (Area Under Profile Curve Ratio)\n", this.result());
        }

        @Override
        protected float totalArea() {
            float studentsWithPreference = 0;
            for (Agent student : this.students.asCollection()) {
                if (student.projectPreference.isCompletelyIndifferent() == false)
                    studentsWithPreference += 1;
            }

            float result = projects.count() * studentsWithPreference;

            // prevent division by zero
            return (result == 0) ? -1 : result;
        }

        @Override
        protected int aupc() {
            int result = 0;
            for (int r = 1; r <= this.projects.count(); r++) {
                for (Match<? extends Group, Project> match : this.matchings.asList()) {
                    AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);
                    for (AssignedProjectRankStudent metric : assignedProjectRank.studentRanks()) {

                        // Student rank -1 indicates no preference, do not include this student
                        if (metric.studentsRank() <= r && metric.studentsRank() != -1) {
                            result += 1;
                        }
                    }
                }
            }

            return result;
        }
    }

    public static class GroupAUPCR extends AUPCR {
        public GroupAUPCR (Matchings<? extends Group, Project> matchings, Projects projects, Agents students) {
            super(matchings, projects, students);
        }

        @Override
        public void printResult() {
            System.out.printf("Groups AUPCR: %f (Area Under Profile Curve Ratio)\n", this.result());
        }

        @Override
        protected float totalArea() {
            int totalProjectCapacity = 0;
            for (Project p : this.projects.asCollection()) {
                totalProjectCapacity += p.slots().size();
            }

            float result = (projects.count() * Math.min(this.matchings.asList().size(), totalProjectCapacity));

            // prevent division by zero
            return (result == 0) ? -1 : result;
        }

        @Override
        protected int aupc() {
            int result = 0;
            for (int r = 1; r <= projects.count(); r++) {
                for (Match<? extends Group, Project> match : matchings.asList()) {
                    AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);
                    if (assignedProjectRank.groupRank() <= r) {
                        result += 1;
                    }
                }
            }

            return result;
        }
    }
}
