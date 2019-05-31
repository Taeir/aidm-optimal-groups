package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ProjectPreference implementation for a whole group. This is an average of the group member preferences (as implemented in BepSYS)
 */
public class AverageProjectPreferenceOfGroup implements ProjectPreference
{
	private Group group;

	private int[] avgPreference;

	public AverageProjectPreferenceOfGroup(Group group)
	{
		this.group = group;
	}

	private int[] calculateAverageOfGroup()
	{
		// mapping: Project --> Preference-rank
		Map<Integer, Integer> prefs = new LinkedHashMap<>();

		int prefCounter = 0;
		for (Agent agent : group.members().asCollection()) {
			int[] preferences = agent.projectPreference.asArray();
			if (preferences.length > 0) {
				prefCounter += 1;
			}

			for (int priority = 0; priority < preferences.length; priority++) {
				int project = preferences[priority];
				int currentPreferences = prefs.getOrDefault(project, 0);
				prefs.put(project, currentPreferences + priority);
			}
		}

		for (Map.Entry<Integer, Integer> entry : prefs.entrySet()) {
			int avgPref = 4;
			if (prefCounter > 0) {
				avgPref = entry.getValue() / prefCounter;
			}
			entry.setValue(avgPref);
		}

		// obtain a list of preferences (id's of) sorted by the rank of the preference
		int[] avgPreference = new ArrayList<>(prefs.entrySet()).stream()
			.sorted(Map.Entry.comparingByValue())
			.map(entry -> entry.getKey())
			.mapToInt(Integer::intValue)
			.toArray();

		return avgPreference;
	}

	@Override
	public int[] asArray()
	{
		if (avgPreference == null) {
			avgPreference = calculateAverageOfGroup();
		}

		return avgPreference;
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
}
