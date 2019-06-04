package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.model.entity.Agents;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;
import nl.tudelft.aidm.optimalgroups.model.entity.Projects;

public abstract class AUPCR {

    protected Matching<Group.FormedGroup, Project.ProjectSlot> matching;
    protected Projects projects;
    protected Agents students;

    protected float aupcr = -1;

    /**
     * Class to implement the Area Under Profile Curve Ratio (AUPCR) metric.
     * An AUPCR of 1 is perfect and an AUPCR of 0 is terrible.
     * Defined on page 8 of (Diebold & Bichler, 2016)
     */
    public AUPCR (Matching<Group.FormedGroup, Project.ProjectSlot> matching, Projects projects, Agents students) {
        this.matching = matching;
        this.projects = projects;
        this.students = students;
    }

    public float result() {
        if (this.aupcr == -1) {
            System.out.printf("result: %d / %d\n", this.aupc(), this.totalArea());
            this.aupcr = ((float) this.aupc()) / this.totalArea(); //TODO: not sure if this works with integer division
        }

        return this.aupcr;
    }

    public abstract void printResult();

    protected abstract int totalArea();

    protected abstract int aupc();

    public static class StudentAUPCR extends AUPCR {
        public StudentAUPCR (Matching<Group.FormedGroup, Project.ProjectSlot> matching, Projects projects, Agents students) {
            super(matching, projects, students);
        }

        @Override
        public void printResult() {
            System.out.printf("Students AUPCR: %f (Area Under Profile Curve Ratio)\n", this.result());
        }

        @Override
        protected int totalArea() {
            int totalProjectCapacity = 0;
            for (Project p : this.projects.asCollection()) {
                totalProjectCapacity += p.slots().size();
            }

            System.out.printf("totalArea: %d * Math.min(%d, %d)\n", this.projects.count(), this.students.count(), totalProjectCapacity);
            int result = projects.count() * Math.min(this.students.count(), totalProjectCapacity);

            // prevent division by zero
            return (result == 0) ? -1 : result;
        }

        @Override
        protected int aupc() {
            int result = 0;
            System.out.printf("aupc: %d projects\n", this.projects.count());
            for (int r = 1; r <= this.projects.count(); r++) {
                for (Matching.Match<Group.FormedGroup, Project.ProjectSlot> match : this.matching.asList()) {
                    AssignedProjectRank assignedProjectRank = new AssignedProjectRank(match);
                    for (AssignedProjectRankStudent metric : assignedProjectRank.studentRanks()) {
                        if (metric.studentsRank() <= r) {
                            result += 1;
                        }
                    }
                }
            }

            System.out.printf("aupc: result: %d\n", result);
            return result;
        }
    }

    public static class GroupAUPCR extends AUPCR {
        public GroupAUPCR (Matching<Group.FormedGroup, Project.ProjectSlot> matching, Projects projects, Agents students) {
            super(matching, projects, students);
        }

        @Override
        public void printResult() {
            System.out.printf("Groups AUPCR: %f (Area Under Profile Curve Ratio)\n", this.result());
        }

        @Override
        protected int totalArea() {
            int totalProjectCapacity = 0;
            for (Project p : this.projects.asCollection()) {
                totalProjectCapacity += p.slots().size();
            }

            int result = (projects.count() * Math.min(this.matching.asList().size(), totalProjectCapacity));

            // prevent division by zero
            return (result == 0) ? -1 : result;
        }

        @Override
        protected int aupc() {
            int result = 0;
            for (int r = 1; r <= projects.count(); r++) {
                for (Matching.Match<Group.FormedGroup, Project.ProjectSlot> match : matching.asList()) {
                    AssignedProjectRank assignedProjectRank = new AssignedProjectRank(match);
                    if (assignedProjectRank.groupRank() <= r) {
                        result += 1;
                    }
                }
            }

            return result;
        }
    }
}
