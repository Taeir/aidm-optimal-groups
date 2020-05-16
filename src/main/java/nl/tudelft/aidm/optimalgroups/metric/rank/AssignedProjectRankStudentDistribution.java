package nl.tudelft.aidm.optimalgroups.metric.rank;

import nl.tudelft.aidm.optimalgroups.metric.Distribution;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public class AssignedProjectRankStudentDistribution extends Distribution
{
    private Matching<? extends Group, Project> matching;

    public AssignedProjectRankStudentDistribution(Matching<? extends Group, Project> matching, Projects projects) {
        super(0.5f, projects.count() + 0.5f, projects.count());
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Match<? extends Group, Project> match : this.matching.asList()) {

            AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);
            for (AssignedProjectRankStudent rank : assignedProjectRank.studentRanks()) {

                // Dont add students without preferences to the distribution
                if (rank.isOfIndifferentAgent())
                    continue;

                // 0 -> unmatched?
                this.addValue(rank.asInt().orElse(0));
            }
        }
    }

    @Override
    public String distributionName() {
        return "Student project rank distribution";
    }
}
