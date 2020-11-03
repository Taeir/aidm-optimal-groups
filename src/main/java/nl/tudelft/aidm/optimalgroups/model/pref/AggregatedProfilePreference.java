package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.Application;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.base.AbstractListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.*;

/**
 * ProjectPreference implementation for a whole group. This is an average of the group member preferences (as implemented in BepSYS)
 */
public abstract class AggregatedProfilePreference extends AbstractListBasedProjectPreferences
{
	protected final Agents agents;

	protected Integer[] avgPreferenceAsArray;
	protected List<Project> avgPreferenceAsProjectList;
	protected Map<Integer, Integer> avgPreferenceMap;

	protected DatasetContext datasetContext;

	public AggregatedProfilePreference(Agents agents)
	{
		this.agents = agents;
		this.datasetContext = agents.datasetContext;
	}

	protected abstract Integer[] calculateAverageOfGroup();

	public Agents agentsAggregatedFrom()
	{
		return agents;
	}

	@Override
	public synchronized Integer[] asArray()
	{
		if (avgPreferenceAsArray == null) {
			avgPreferenceAsArray = calculateAverageOfGroup();
		}

		return avgPreferenceAsArray;
	}

	@Override
	public synchronized List<Project> asListOfProjects()
	{
		if (avgPreferenceAsProjectList == null) {
			var projectIdsInOrder = asArray();

			Projects allProjects = datasetContext.allProjects();
			List<Project> projectList = new ArrayList<>(projectIdsInOrder.length);

			for (var projId : projectIdsInOrder) {
				projectList.add(allProjects.findWithId(projId).get());
			}

			avgPreferenceAsProjectList = Collections.unmodifiableList(projectList);
		}

		return avgPreferenceAsProjectList;
	}

	public static AggregatedProfilePreference usingGloballyConfiguredMethod(Agents agents)
	{
		if (Application.preferenceAggregatingMethod.equals("Copeland"))
		{
			return new AggregatedProfilePreference.Copeland(agents);
		}
		else if (Application.preferenceAggregatingMethod.equals("Borda"))
		{
			return new AggregatedProfilePreference.Borda(agents);
		}
		else
		{
			throw new RuntimeException("Unrecognized aggregation method, was: " + Application.preferenceAggregatingMethod);
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ProjectPreference)) return false;
		if (!(o instanceof AggregatedProfilePreference)) throw new RuntimeException("Hmm AggregatedProfilePreference is being compared with some other type. Check if use-case is alright.");
		AggregatedProfilePreference that = (AggregatedProfilePreference) o;
		return Arrays.equals(avgPreferenceAsArray, that.avgPreferenceAsArray);
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(avgPreferenceAsArray);
	}

	public static class Borda extends AggregatedProfilePreference
	{
		public Borda(Agents agents)
		{
			super(agents);
		}

		@Override
		protected Integer[] calculateAverageOfGroup()
		{
			// mapping: Project --> Preference-rank
			Map<Integer, Integer> prefs = new LinkedHashMap<>();

			for (Agent agent : this.agents.asCollection()) {
				Integer[] preferences = agent.projectPreference().asArray();

				for (int priority = 0; priority < preferences.length; priority++) {
					int project = preferences[priority];
					int currentPreferences = prefs.getOrDefault(project, 0);
					prefs.put(project, currentPreferences + priority);
				}
			}

			// obtain a list of preferences (id's of) sorted by the rank of the preference
			Integer[] avgPreference = new ArrayList<>(prefs.entrySet()).stream()
					.sorted(Map.Entry.comparingByValue())
					.map(Map.Entry::getKey)
					.toArray(Integer[]::new);

			return avgPreference;
		}
	}

	public static class Copeland extends AggregatedProfilePreference
	{
		public Copeland(Agents agents)
		{
			super(agents);
		}

		@Override
		protected Integer[] calculateAverageOfGroup()
		{
			Set<Integer> projects = null;

			// Retrieve the projects
			for (Agent agent : this.agents.asCollection()) {
				Integer[] preferences = agent.projectPreference().asArray();
				if (preferences.length > 0) {
					projects = agent.projectPreference().asMap().keySet();
					break;
				}
			}

			if (projects == null) {
				return new Integer[0];
			}

			// Start comparing projects
			Map<Integer, Map<Integer, Boolean>> pairwiseComparison = new HashMap<>(projects.size());
			for (int project : projects) {

				HashMap<Integer, Boolean> currentComparison = new HashMap<>(projects.size());

				for (int compareProject : projects) {
					if (project == compareProject)
						continue;

					int wins = 0;
					int defeats = 0;
					for (Agent a : this.agents.asCollection()) {
						Map<Integer, Integer> preferences = a.projectPreference().asMap();
						if (preferences.get(project) == null || preferences.get(compareProject) == null) {
							continue;
						}

						if (preferences.get(project) < preferences.get(compareProject)) {
							wins++;
						} else {
							defeats++;
						}
					}

					if (wins > defeats) {
						currentComparison.put(compareProject, true);
					} else if (wins < defeats) {
						currentComparison.put(compareProject, false);
					} else {
						// Its a tie, does not matter if it gets put as true or false in map
						currentComparison.put(compareProject, false);
					}
				}

				pairwiseComparison.put(project, currentComparison);
			}

			// Score here means the amount of wins minus the amount of defeats.
			// Note that ties get denoted as losses in the above algorithm but as long as this is done consistently (which it is)
			//  then it does not matter.
			Map<Integer, Integer> projectScore = new HashMap<>();
			for (Map.Entry<Integer, Map<Integer, Boolean>> entry : pairwiseComparison.entrySet()) {

				int project = entry.getKey();
				Map<Integer, Boolean> comparisonResult = entry.getValue();

				// Start neutral
				projectScore.put(project, 0);
				for (boolean win : comparisonResult.values()) {

					// Increment or decrement the value based on win or loss
					if (win) {
						projectScore.put(project, projectScore.get(project) + 1);
					} else {
						projectScore.put(project, projectScore.get(project) - 1);
					}
				}
			}

			// obtain a list of preferences (id's of) sorted by the rank of the preference
			Integer[] avgPreference = new ArrayList<>(projectScore.entrySet()).stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.map(Map.Entry::getKey)
					.toArray(Integer[]::new);

			return avgPreference;
		}
	}
}
