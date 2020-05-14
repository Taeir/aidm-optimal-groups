package nl.tudelft.aidm.optimalgroups.metric.dataset.projprefbinning;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: introduce concept of a bin. A bin can be either an exact ordering, a top x ordering or fuzzy (trees?)
public class ProjectPreferenceProfileFrequencies
{
	private final Map<BinnableProjPref, Long> preferenceFrequencies;

	public ProjectPreferenceProfileFrequencies(Agents agents, Function<ProjectPreference, BinnableProjPref> binner)
	{
		this.preferenceFrequencies = agents.asCollection().stream()
			.map(Agent::projectPreference)
			.map(binner)
			.collect(
				Collectors.groupingBy(bin -> bin,
					Collectors.counting()
				)
			);
	}

	public List<Pair<BinnableProjPref, Long>> mostPopular(int limit)
	{
		return this.preferenceFrequencies.entrySet().stream()
			.sorted(Map.Entry.<BinnableProjPref, Long>comparingByValue().reversed())
			.limit(limit)
			.map(entry ->
				new Pair<>(
					entry.getKey(),
					entry.getValue()
				)
			)
			.collect(Collectors.toList());
	}
}
