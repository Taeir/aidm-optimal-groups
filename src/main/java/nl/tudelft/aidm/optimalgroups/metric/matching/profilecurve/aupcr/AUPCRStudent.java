package nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankGroup;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public class AUPCRStudent extends AUPCR {

    public AUPCRStudent(Matching<? extends Group, Project> matching) {
        this(matching, matching.datasetContext().allProjects(), matching.datasetContext().allAgents());
    }

    public AUPCRStudent(Matching<? extends Group, Project> matching, Projects projects, Agents students) {
        super(matching, projects, students);
    }

    @Override
    public void printResult() {
        System.out.printf("Students AUPCR: %f (Area Under Profile Curve Ratio)\n", this.asDouble());
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
            for (Match<? extends Group, Project> match : this.matching.asList()) {
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
