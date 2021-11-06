package nl.tudelft.aidm.optimalgroups.metric.rank;

import nl.tudelft.aidm.optimalgroups.model.HasProjectPrefs;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public record SumOfRanks(Matching<? extends HasProjectPrefs, Project> matching)
{
	public int asInt()
	{
		return matching.asList().stream()
				.map(match -> match.from().projectPreference().rankOf(match.to()))
				.filter(rankInPref -> !rankInPref.isCompletelyIndifferent())
				.mapToInt(rankInPref -> rankInPref.unacceptable() ? Integer.MAX_VALUE : rankInPref.asInt())
				.sum();
	}
}
