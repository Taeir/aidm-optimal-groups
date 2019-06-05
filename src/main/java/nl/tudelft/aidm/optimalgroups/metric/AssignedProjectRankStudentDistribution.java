package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

public class AssignedProjectRankStudentDistribution extends Distribution{
    private Matching<Group.FormedGroup, Project.ProjectSlot> matching;

    public AssignedProjectRankStudentDistribution(Matching<Group.FormedGroup, Project.ProjectSlot> matching, int projectAmount) {
        super(0.5f, projectAmount + 0.5f, projectAmount);
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Matching.Match<Group.FormedGroup, Project.ProjectSlot> match : this.matching.asList()) {

            AssignedProjectRank assignedProjectRank = new AssignedProjectRank(match);
            for (AssignedProjectRankStudent rank : assignedProjectRank.studentRanks()) {

                //Dont add students without preferences to the distribution
                if (rank.studentsRank() == -1)
                    continue;

                this.addValue(rank.studentsRank());
            }
        }
    }

    @Override
    protected String distributionName() {
        return "Student project rank distribution";
    }
}
