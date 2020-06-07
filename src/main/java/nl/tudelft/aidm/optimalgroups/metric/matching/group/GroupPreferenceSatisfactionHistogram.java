package nl.tudelft.aidm.optimalgroups.metric.matching.group;

import nl.tudelft.aidm.optimalgroups.metric.Histogram;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class GroupPreferenceSatisfactionHistogram extends Histogram
{

    private Matching<Group.FormedGroup, Project> matching;

    public GroupPreferenceSatisfactionHistogram(Matching<Group.FormedGroup, Project> matching, int partitionAmount) {
        super(0, 1, partitionAmount);
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Match<Group.FormedGroup, Project> match : this.matching.asList()) {
            Group.FormedGroup group = match.from();
            group.members().forEach(student -> {
                PeerPreferenceSatisfaction preferenceSatisfaction = new PeerPreferenceSatisfaction(group, student);
                boolean added = this.addValue(preferenceSatisfaction.asFloat());
            });
        }
    }

    @Override
    public String distributionName() {
        return "Group preference satisfaction distribution";
    }
}
