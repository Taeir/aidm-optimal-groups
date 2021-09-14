package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.PresentRankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.UnacceptableAlternativeRank;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.*;

public class CombinedPreference implements ProjectPreference {

    private final int GROUP_PREFERENCE_WEIGHT = 1;
    private final int PROJECT_PREFERENCE_WEIGHT = 1;

    private final GroupPreference groupPreference;
    private final ProjectPreference projectPreference;
    private final Agents agents;

    private Project[] asArray = null;
    private List<Project> asList = null;
    private Map<Project, RankInPref> asMap;
    
    // The combined preference is a ranking of projects, based on both
    // the project preferences from the and the group preferences from the database

    public CombinedPreference(GroupPreference gp, ProjectPreference pp, Agents a) {
        this.groupPreference = gp;
        this.projectPreference = pp;
        this.agents = a;
    }

    @Override
    public Object owner()
    {
        return projectPreference.owner();
    }

    @Override
    public Project[] asArray() {
        if (this.asArray == null) {
            this.asArray = this.asList().toArray(Project[]::new);
        }

        return this.asArray;
    }

    @Override
    public synchronized List<Project> asList()
    {
        if (asList == null) {
            asList = this.asMap().entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .collect(toList());
        }

        return asList;
    }
    
    @Override
    public RankInPref rankOf(Project project)
    {
        return asMap()
                .computeIfAbsent(project, p -> new UnacceptableAlternativeRank(owner(), p));
    }
    
    @Override
    public Map<Project, RankInPref> asMap()
    {
        if (this.asMap != null)
            return this.asMap;
        
        // Here we compute the combined preference
        
        // If no group preference is given, there is nothing to combine so just return the project preference
        if (this.groupPreference.count() == 0) {
            return projectPreference.asMap();
        }
        
        // Higher score = more preferred
        final Map<Project, Double> scores = new HashMap<>();

        // Divide the preference weight of the whole group preference by the amount of peers
        // to get a weight of the groupPreference of an individual student
        final float groupPreferenceWeightPerPeer = ((float) this.GROUP_PREFERENCE_WEIGHT) / this.groupPreference.asArray().length;

        for (var peer : this.groupPreference.asArray()) {
            
            // Peer is indifferent, thus it cannot impact the preferences of this agent, that is
            // this agent's project preferences need not be modified to attempt to be matched together
            // Also note that the forEach below will simply have no effect if the agent is indifferent,
            // this if-statment is for explicit documentational purposes
            if (peer.projectPreference().isCompletelyIndifferent()) {
                continue;
            }
            
            peer.projectPreference().forEach((project, rankInPref, __) -> {
                
                // Business rule decision (ours): do not adjust preferences for
                // when friend considers project to be unacceptible. Being placed with all your peers is not
                // guaranteed by this combined-pref approach, so if this agent find the project acceptible, but some other not,
                // we let the project be as-is for this agent.
                // -- An alternative decision is to penalize the score of the project instead,
                // such that the project preferences become a sort of intersection between everyone's preferences
                // but if this is desirable, then it's easier to simply do this scoring procedure on the proper intersection.
                if (rankInPref.isPresent()) {
                    var rankAsInt = rankInPref.asInt();
                    
                    var score = (1d / rankAsInt) * groupPreferenceWeightPerPeer;
                    scores.merge(project, score, Double::sum);
                }
            });
            
            // OLD IMPL - based on complete, total orderings over projects, but the assumption on complete and total is no longer valid
            // Keeping the code here for reference in case the above is broken
            /* for (int rank = 0; rank < peerProjectPreferences.length; rank++) {
                int project = peerProjectPreferences[rank];

                // Calculate the amount that this peer is going to add to the score of this project,
                // do the (length-rank) subtraction to achieve the result that a high score means more preferred
                float score = ((float) (peerProjectPreferences.length - rank)) * groupPreferenceWeightPerPeer;

                // Update the score of this project
                float currentScore = scores.getOrDefault(project, 0.0f);
                scores.put(project, currentScore + score);
            }*/
        }

        // Add the student's own project preference to the scores
            
        this.projectPreference.forEach((project, rank, __) -> {
            
            double score;
            
            if (rank.unacceptable())
                score = 0;
            else
                score = 1d / rank.asInt() * PROJECT_PREFERENCE_WEIGHT;
            
            scores.merge(project, score, Double::sum);
        });
        
        // Convert into score -> project mapping
        var mapScoreToProject = scores.entrySet().stream()
                .collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toList())));
        
        var orderedScores = mapScoreToProject.keySet().stream()
                .sorted()
                .collect(toList());
        
        this.asMap = mapScoreToProject.entrySet().stream()
                .flatMap(entry -> {
                    var score = entry.getKey();
                    var projects = entry.getValue();
                    
                    return projects.stream().map(project -> {
                        RankInPref rank;
                        
                        if (score == 0) {
                            rank = new UnacceptableAlternativeRank(owner(), project);
                        }
                        else {
                            var rankAsInt = orderedScores.indexOf(score);
                            Assert.that(rankAsInt >= 0).orThrowMessage("BUGCHECK: Rank of score could not be found");
                            rank = new PresentRankInPref(rankAsInt);
                        }
                        
                        return Map.entry(project, rank);
                    });
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        return this.asMap;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o instanceof ProjectPreference) {
            if (o instanceof CombinedPreference) {
                CombinedPreference that = (CombinedPreference) o;
                return projectPreference.equals(that.projectPreference);
            }
            else {
                throw new RuntimeException("Hmm CombinedPref is being compared with some other type. Check if use-case is alright.");
//                ProjectPreference that = (ProjectPreference) o;
//                return projectPreference.equals(that);
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(projectPreference);
    }
}
