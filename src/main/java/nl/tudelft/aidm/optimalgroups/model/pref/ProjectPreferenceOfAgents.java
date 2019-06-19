package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.Application;
import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Agents;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ProjectPreference implementation for a whole group. This is an average of the group member preferences (as implemented in BepSYS)
 */
public abstract class ProjectPreferenceOfAgents implements ProjectPreference
{
	protected Agents agents;

	protected int[] avgPreference;
	protected Map<Integer, Integer> avgPreferenceMap;

	public ProjectPreferenceOfAgents(Agents agents)
	{
		this.agents = agents;
	}

	abstract int[] calculateAverageOfGroup();

	@Override
	public int[] asArray()
	{
		if (avgPreference == null) {
			avgPreference = calculateAverageOfGroup();
		}

		return avgPreference;
	}

	@Override
	public Map<Integer, Integer> asMap() {
		if (this.avgPreferenceMap == null) {
			this.avgPreferenceMap = new HashMap<>();

			int[] preferences = asArray();
			for (int rank = 1; rank <= preferences.length; rank++) {
				this.avgPreferenceMap.put(preferences[rank - 1], rank);
			}
		}

		return this.avgPreferenceMap;
	}

	@Override
	public void forEach(ProjectPreferenceConsumer iter)
	{
		int[] prefs = asArray();
		for (int i = 0; i < prefs.length; i++)
		{
			iter.apply(prefs[i], i+1);
		}
	}

	public static ProjectPreferenceOfAgents aggregateWithGloballyConfiguredAggregationMethod(Agents agents) {
		if (Application.preferenceAggregatingMethod.equals("Copeland")) {
			return new ProjectPreferenceOfAgents.Copeland(agents);
		} else {
			return new ProjectPreferenceOfAgents.Borda(agents);
		}
	}

	public static class Borda extends ProjectPreferenceOfAgents {

		public Borda(Agents agents) {
			super(agents);
		}

		@Override
		int[] calculateAverageOfGroup() {
			// mapping: Project --> Preference-rank
			Map<Integer, Integer> prefs = new LinkedHashMap<>();

			for (Agent agent : this.agents.asCollection()) {
				int[] preferences = agent.getProjectPreference().asArray();

				for (int priority = 0; priority < preferences.length; priority++) {
					int project = preferences[priority];
					int currentPreferences = prefs.getOrDefault(project, 0);
					prefs.put(project, currentPreferences + priority);
				}
			}

			// obtain a list of preferences (id's of) sorted by the rank of the preference
			int[] avgPreference = new ArrayList<>(prefs.entrySet()).stream()
					.sorted(Map.Entry.comparingByValue())
					.map(entry -> entry.getKey())
					.mapToInt(Integer::intValue)
					.toArray();

			return avgPreference;
		}
	}

	public static class Copeland extends ProjectPreferenceOfAgents {

		public Copeland(Agents agents) {
			super(agents);
		}

		@Override
		int[] calculateAverageOfGroup() {
			Set<Integer> projects = null;

			// Retrieve the projects
			for (Agent agent : this.agents.asCollection()) {
				int[] preferences = agent.getProjectPreference().asArray();
				if (preferences.length > 0) {
					projects = agent.getProjectPreference().asMap().keySet();
					break;
				}
			}

			if (projects == null)
				return new int[0];

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
						Map<Integer, Integer> preferences = a.getProjectPreference().asMap();
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
			int[] avgPreference = new ArrayList<>(projectScore.entrySet()).stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.map(entry -> entry.getKey())
					.mapToInt(Integer::intValue)
					.toArray();

			return avgPreference;
		}
	}
}
