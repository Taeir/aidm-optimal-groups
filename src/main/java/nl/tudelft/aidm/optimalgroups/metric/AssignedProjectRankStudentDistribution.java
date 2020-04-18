package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matchings;
import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class AssignedProjectRankStudentDistribution extends Distribution{
    private Matchings<? extends Group, Project> matchings;

    public AssignedProjectRankStudentDistribution(Matchings<? extends Group, Project> matchings, int projectAmount) {
        super(0.5f, projectAmount + 0.5f, projectAmount);
        this.matchings = matchings;
    }

    @Override
    protected void calculate() {
        for (Match<? extends Group, Project> match : this.matchings.asList()) {

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
