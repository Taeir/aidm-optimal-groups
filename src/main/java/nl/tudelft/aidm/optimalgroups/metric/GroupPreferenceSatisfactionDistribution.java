package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matchings;
import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.Project;

public class GroupPreferenceSatisfactionDistribution extends Distribution {

    private Matchings<Group.FormedGroup, Project> matchings;

    public GroupPreferenceSatisfactionDistribution(Matchings<Group.FormedGroup, Project> matchings, int partitionAmount) {
        super(0, 1, partitionAmount);
        this.matchings = matchings;
    }

    @Override
    protected void calculate() {
        for (Match<Group.FormedGroup, Project> match : this.matchings.asList()) {
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
