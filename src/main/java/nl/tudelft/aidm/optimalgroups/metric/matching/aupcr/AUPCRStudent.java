package nl.tudelft.aidm.optimalgroups.metric.matching.aupcr;

import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.stream.Collectors;

public class AUPCRStudent extends AUPCR {

    private final Matching<Agent, Project> matching;
    private final Projects projects;
    private final Agents students;
    
    public AUPCRStudent(Matching<Agent, Project> matching)
    {
        this(matching, matching.datasetContext().allProjects(), agentsIn(matching));
    }
    
    private static Agents agentsIn(Matching<Agent, Project> matching)
    {
        return matching.asList().stream().map(Match::from).distinct().collect(Agents.collector);
    }
    
    public AUPCRStudent(Matching<Agent, Project> matching, Agents agents)
    {
        this(matching, matching.datasetContext().allProjects(), agents);
    }

    public AUPCRStudent(Matching<Agent, Project> matching, Projects projects, Agents students) {
        this.matching = matching;
        this.projects = projects;
        this.students = students;
    }

    @Override
    public void printResult() {
        System.out.printf("Students AUPCR: %f\n", this.asDouble());
    }

    @Override
    protected float totalArea()
    {
        // Ignore indifferent agents - they have no ranks
        var numStudentsIndifferent = this.students.asCollection().stream()
                .filter(agent -> agent.projectPreference().isCompletelyIndifferent())
                .count();
        
        var numStudentsWithPreferences = this.students.count() - numStudentsIndifferent;

        float result = projects.count() * numStudentsWithPreferences;

        // prevent division by zero
        return (result == 0) ? -1 : result;
    }

    @Override
    protected int aupc() {
        int result = 0;

        var groupedByRank = matching.asList().stream()
                .map(match -> {
                    var student = match.from();
                    var assignedProject = match.to();
                    
                    var rank = student.projectPreference().rankOf(assignedProject);
                    return rank;
                })
                .filter(RankInPref::isPresent)
                .map(RankInPref::asInt)
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
        
        // cum-sum ranks
        for (int r = 1; r <= this.projects.count(); r++) {
            for (int i = 1; i <= r; i++) {
                result += groupedByRank.getOrDefault(i, 0L);
            }
        }

        return result;
    }
}
