package nl.tudelft.aidm.optimalgroups.metric.rank;

import nl.tudelft.aidm.optimalgroups.metric.Distribution;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public class AssignedProjectRankGroupDistribution extends Distribution
{

    private Matching<? extends Group, Project> matching;

    public AssignedProjectRankGroupDistribution(Matching<? extends Group, Project> matching, Projects projects) {
        super(0.5f, projects.count() + 0.5f, projects.count());
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Match<? extends Group, Project> match : this.matching.asList()) {
            AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);

            // Dont add groups without preferences to the distribution
            if (assignedProjectRank.isOfIndifferentAgent())
                continue;

            // 0 -> unmatched?
            this.addValue(assignedProjectRank.asInt().orElse(0));
        }
    }

    @Override
    public String distributionName() {
        return "Group project rank distribution";
    }
}
