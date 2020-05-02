package nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankGroup;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public class AUPCRGroup extends AUPCR {
    public AUPCRGroup(Matching<? extends Group, Project> matching, Projects projects, Agents students) {
        super(matching, projects, students);
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

        float result = (projects.count() * Math.min(this.matching.asList().size(), totalProjectCapacity));

        // prevent division by zero
        return (result == 0) ? -1 : result;
    }

    @Override
    protected int aupc() {
        int result = 0;
        for (int r = 1; r <= projects.count(); r++) {
            for (Match<? extends Group, Project> match : matching.asList()) {
                AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);
                if (assignedProjectRank.groupRank() <= r) {
                    result += 1;
                }
            }
        }

        return result;
    }
}
