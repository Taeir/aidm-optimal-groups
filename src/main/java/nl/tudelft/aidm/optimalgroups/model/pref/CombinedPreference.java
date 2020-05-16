package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.*;

public class CombinedPreference implements ProjectPreference {

    private final int GROUP_PREFERENCE_WEIGHT = 1;
    private final int PROJECT_PREFERENCE_WEIGHT = 1;

    private final GroupPreference groupPreference;
    private final ProjectPreference projectPreference;
    private final Agents agents;

    private Integer[] combinedPreference = null;
    private List<Project> combinedPreferenceAsProjectList = null;
    private Map<Integer, Integer> combinedPreferenceMap = null;

    // The combined preference is a ranking of projects, based on both
    // the project preferences from the and the group preferences from the database

    public CombinedPreference(GroupPreference gp, ProjectPreference pp, Agents a) {
        this.groupPreference = gp;
        this.projectPreference = pp;
        this.agents = a;
    }

    @Override
    public Integer[] asArray() {
        if (this.combinedPreference == null) {
            this.combinedPreference = this.computeCombinedPreference();
        }

        return this.combinedPreference;
    }

    @Override
    public synchronized List<Project> asListOfProjects()
    {
        if (combinedPreferenceAsProjectList == null) {
            var projectIdsInOrder = asArray();

            Projects projects = Projects.from(projectPreference.asListOfProjects());
            List<Project> projectList = new ArrayList<>(projectIdsInOrder.length);

            for (var projId : projectIdsInOrder) {
                projectList.add(projects.findWithId(projId).get());
            }

            combinedPreferenceAsProjectList = Collections.unmodifiableList(projectList);
        }

        return combinedPreferenceAsProjectList;
    }

    @Override
    public Map<Integer, Integer> asMap() {
        if (this.combinedPreferenceMap == null) {
            Integer[] preferences = this.asArray();
            this.combinedPreferenceMap = new HashMap<>(preferences.length);

            for (int rank = 1; rank <= preferences.length; rank++) {
                int project = preferences[rank - 1];
                this.combinedPreferenceMap.put(project, rank);
            }
        }

        return this.combinedPreferenceMap;
    }

    private Integer[] computeCombinedPreference() {
        // If no group preference is given, there is nothing to combine so just return the raw project preferences
        if (this.groupPreference == null ||  this.groupPreference.asArray().length == 0)
            return this.projectPreference.asArray();

        // The result will rank all the projects so create an array with length of the project amount
        Integer[] result = new Integer[this.projectPreference.asArray().length];

        // Keep the scores of the project in a map where the key is the project id and the value is the score
        Map<Integer, Float> scores = new HashMap<>();

        // Divide the preference weight of the whole group preference by the amount of peers
        // to get a weight of the groupPreference of an individual student
        final float groupPreferenceWeightPerPeer = ((float) this.GROUP_PREFERENCE_WEIGHT) / this.groupPreference.asArray().length;

        for (int peerId : this.groupPreference.asArray()) {

            // Get the agent object belonging to the peer and its project preferences (or throw exception if it is not in collection)
            Agent peer = this.agents.findByAgentId(peerId).get();
            Integer[] peerProjectPreferences = peer.projectPreference().asArray();

            for (int rank = 0; rank < peerProjectPreferences.length; rank++) {
                int project = peerProjectPreferences[rank];

                // Calculate the amount that this peer is going to add to the score of this project,
                // do the (length-rank) subtraction to achieve the result that a high score means much preferred
                float score = ((float) (peerProjectPreferences.length - rank)) * groupPreferenceWeightPerPeer;

                // Update the score of this project
                float currentScore = scores.getOrDefault(project, 0.0f);
                scores.put(project, currentScore + score);
            }

        }

        // Add the students own project preference to the scores
        Integer[] ownProjectPreferences = this.projectPreference.asArray();

        for (int rank = 0; rank < ownProjectPreferences.length; rank++) {
            int project = ownProjectPreferences[rank];

            // Calculate the amount that this project is going to add to the combined score of the project,
            // do the (length-rank) subtraction to achieve the result that a high score means much preferred
            float score = ((float) (ownProjectPreferences.length - rank)) * this.PROJECT_PREFERENCE_WEIGHT;

            // Update the score of this project.
            float currentScore = scores.getOrDefault(project, 0.0f);
            scores.put(project, currentScore + score);
        }

        // Create a PriorityQueue of map entries, that gets sorted on the score of projects.
        // Do the reversed comparison to let a high float value get priority over low float values
        PriorityQueue<Map.Entry<Integer, Float>> projectsOrdered = new PriorityQueue<>(Comparator.comparing((Map.Entry<Integer, Float> e) -> e.getValue()).reversed());

        // Add all projects to the PriorityQueue
        for (Map.Entry<Integer, Float> entry : scores.entrySet()) {
            projectsOrdered.add(entry);
        }

        // Fill the result array with the ordered projects from the PriorityQueue
        for (int i = 0; i < result.length; i++) {
            Map.Entry<Integer, Float> projectEntry = projectsOrdered.poll();
            int project = projectEntry.getKey();
            result[i] = project;
        }

        return result;
    }

    public void forEach(CombinedPreference.CombinedPreferenceConsumer iter)
    {
        Integer[] prefArray = asArray();
        for (int i = 0; i < prefArray.length; i++)
        {
            iter.apply(prefArray[i], i+1);
        }
    }

    interface CombinedPreferenceConsumer
    {
        /**
         * @param projectId the id of the project that has the given rank
         * @param rank Rank of the preference, 1 being highest
         */
        void apply(int projectId, int rank);
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
