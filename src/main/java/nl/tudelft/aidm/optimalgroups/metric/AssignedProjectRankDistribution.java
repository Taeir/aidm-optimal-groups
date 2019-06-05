package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

public class AssignedProjectRankDistribution extends Distribution {

    private Matching<Group.FormedGroup, Project.ProjectSlot> matching;

    public AssignedProjectRankDistribution(Matching<Group.FormedGroup, Project.ProjectSlot> matching, int projectAmount) {
        super(0.5f, projectAmount + 0.5f, projectAmount);
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Matching.Match<Group.FormedGroup, Project.ProjectSlot> match : this.matching.asList()) {
            AssignedProjectRank assignedProjectRank = new AssignedProjectRank(match);

            //Dont add groups without preferences to the distribution
            if (assignedProjectRank.groupRank() == -1)
                continue;

            this.addValue(assignedProjectRank.groupRank());
        }
    }

    @Override
    protected String distributionName() {
        return "Group project rank distribution";
    }
}
