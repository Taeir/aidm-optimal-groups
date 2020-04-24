package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class AssignedProjectRankStudentDistribution extends Distribution{
    private Matching<? extends Group, Project> matching;

    public AssignedProjectRankStudentDistribution(Matching<? extends Group, Project> matching, int projectAmount) {
        super(0.5f, projectAmount + 0.5f, projectAmount);
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Match<? extends Group, Project> match : this.matching.asList()) {

            AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);
            for (AssignedProjectRankStudent rank : assignedProjectRank.studentRanks()) {

                //Dont add students without preferences to the distribution
                if (rank.studentsRank() == -1)
                    continue;

                this.addValue(rank.studentsRank());
            }
        }
    }

    @Override
    public String distributionName() {
        return "Student project rank distribution";
    }
}
