package nl.tudelft.aidm.optimalgroups.metric.rank.histrogram;

import nl.tudelft.aidm.optimalgroups.metric.Histogram;
import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public class AssignedProjectRankStudentHistogram extends Histogram
{
    private Matching<? extends Group, Project> matching;

    public AssignedProjectRankStudentHistogram(Matching<? extends Group, Project> matching, Projects projects) {
        super(0.5f, projects.count() + 0.5f, projects.count());
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Match<? extends Group, Project> match : this.matching.asList()) {

            AssignedRank.ProjectToGroup assignedProjectRank = new AssignedRank.ProjectToGroup(match);
            for (AssignedRank.ProjectToStudent rank : assignedProjectRank.studentRanks()) {

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
        return "Student project rank histogram";
    }
}
