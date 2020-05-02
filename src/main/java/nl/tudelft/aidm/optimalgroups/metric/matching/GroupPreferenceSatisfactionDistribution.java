package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.Distribution;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class GroupPreferenceSatisfactionDistribution extends Distribution
{

    private Matching<Group.FormedGroup, Project> matching;

    public GroupPreferenceSatisfactionDistribution(Matching<Group.FormedGroup, Project> matching, int partitionAmount) {
        super(0, 1, partitionAmount);
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Match<Group.FormedGroup, Project> match : this.matching.asList()) {
            match.from().members().forEach(student -> {
                GroupPreferenceSatisfaction preferenceSatisfaction = new GroupPreferenceSatisfaction(match, student);
                boolean added = this.addValue(preferenceSatisfaction.asFloat());
            });
        }
    }

    @Override
    public String distributionName() {
        return "Group preference satisfaction distribution";
    }
}
