package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

public class AssignedProjectRankGroupDistribution extends Distribution {

    private Matching<? extends Group, Project> matching;

    public AssignedProjectRankGroupDistribution(Matching<? extends Group, Project> matching, int projectAmount) {
        super(0.5f, projectAmount + 0.5f, projectAmount);
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Matching.Match<? extends Group, Project> match : this.matching.asList()) {
            AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);

            //Dont add groups without preferences to the distribution
            if (assignedProjectRank.groupRank() == -1)
                continue;

            this.addValue(assignedProjectRank.groupRank());
        }
    }

    @Override
    public String distributionName() {
        return "Group project rank distribution";
    }
}
