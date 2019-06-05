package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

import java.util.Map;

public class GroupPreferenceSatisfactionDistribution extends Distribution {

    private Matching<Group.FormedGroup, Project.ProjectSlot> matching;

    public GroupPreferenceSatisfactionDistribution(Matching<Group.FormedGroup, Project.ProjectSlot> matching, int partitionAmount) {
        super(0, 1, partitionAmount);
        this.matching = matching;
    }

    @Override
    protected void calculate() {
        for (Matching.Match<Group.FormedGroup, Project.ProjectSlot> match : this.matching.asList()) {
            match.from().members().forEach(student -> {
                GroupPreferenceSatisfaction preferenceSatisfaction = new GroupPreferenceSatisfaction(match, student);
                boolean added = this.addValue(preferenceSatisfaction.asFloat());
            });
        }
    }
}
