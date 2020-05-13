package nl.tudelft.aidm.optimalgroups.metric.dataset;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

// TODO: introduce concept of a bin. A bin can be either an exact ordering, a top x ordering or fuzzy (trees?)
public class ProjectPreferenceProfileFrequencies
{
	private final Map<ProjectPreference, Long> preferenceFrequencies;

	public ProjectPreferenceProfileFrequencies(Agents agents)
	{
		this.preferenceFrequencies = agents.asCollection().stream()
			.collect(
				Collectors.groupingBy(Agent::projectPreference,
					Collectors.counting()
				)
			);
	}

	public List<Pair<FormattableProjectPreference, Long>> mostPopular(int limit)
	{
		return this.preferenceFrequencies.entrySet().stream()
			.sorted(Map.Entry.<ProjectPreference, Long>comparingByValue().reversed())
			.limit(limit)
			.map(entry ->
				new Pair<>(
					new FormattableProjectPreference(entry.getKey()),
					entry.getValue()
				)
			)
			.collect(Collectors.toList());
	}

	public static ProjectPreferenceProfileFrequencies in(DatasetContext datasetContext)
	{
		return new ProjectPreferenceProfileFrequencies(datasetContext.allAgents());
	}

	public static class FormattableProjectPreference
	{
		private final ProjectPreference projectPreference;

		public FormattableProjectPreference(ProjectPreference projectPreference)
		{
			this.projectPreference = projectPreference;
		}

		public String asLinearOrderInString(int topProjectsLimit)
		{
			Integer[] prefsInArray = projectPreference.asArray();
			return Arrays.stream(Arrays.copyOf(prefsInArray, Math.min(prefsInArray.length, topProjectsLimit)))
				.map(String::valueOf)
				.collect(Collectors.joining(">"));
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof FormattableProjectPreference)) return false;
			FormattableProjectPreference that = (FormattableProjectPreference) o;
			return projectPreference.equals(that.projectPreference);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(projectPreference);
		}
	}
}
