package nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public class AUPCRStudent extends AUPCR {

    private final Matching<Agent, Project> matching;
    private final Projects projects;
    private final Agents students;

    public AUPCRStudent(Matching<Agent, Project> matching) {
        this(matching, matching.datasetContext().allProjects(), matching.datasetContext().allAgents());
    }

    public AUPCRStudent(Matching<Agent, Project> matching, Projects projects, Agents students) {

        this.matching = matching;
        this.projects = projects;
        this.students = students;
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

        // TODO: move to using AssignedProjectRankStudent.inStudentMatching stream
        for (int r = 1; r <= this.projects.count(); r++) {
            for (var match : this.matching.asList()) {
                var rank = new AssignedProjectRankStudent(match);

                // Student rank -1 indicates no preference, do not include this student
                if (rank.asInt() <= r && rank.asInt() != -1) {
                    result += 1;
                }
            }
        }

        return result;
    }
}
