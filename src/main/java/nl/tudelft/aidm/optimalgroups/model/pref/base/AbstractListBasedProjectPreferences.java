package nl.tudelft.aidm.optimalgroups.model.pref.base;

import nl.tudelft.aidm.optimalgroups.metric.rank.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.PresentRankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankOfCompletelyIndifferentAgent;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.UnacceptableAlternativeRank;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A base implementation of ProjectPreferences where an agent has ordinal preference
 * over the projects. The preference can be incomplete, any missing projects are assumed
 * to be tied and given the ordinal rank after those present. So [A > B > C], then rank of
 * D, E, F etc would be 4.
 */
public abstract class AbstractListBasedProjectPreferences implements ProjectPreference
{
	private Project[] asArray = null;
	private Map<Project, RankInPref> asMap;

	private final IdentityHashMap<Project, RankInPref> rankOfProject = new IdentityHashMap<>();

	@Override
	public Project[] asArray()
	{
		if (asArray == null) {
			asArray = asList().toArray(Project[]::new);
		}

		return asArray;
	}

	@Override
	public RankInPref rankOf(Project project)
	{
		// Cache results - pessimism makes heavy use of this fn
		return rankOfProject.computeIfAbsent(project, p ->
		{
			if (isCompletelyIndifferent()) {
				return new RankOfCompletelyIndifferentAgent(owner(), project);
			}

			var rankInArray = new RankInArray().determineRank(p.sequenceNum(), asArray());

			// Alternative is missing, hence it is unacceptable to agent
			if (rankInArray.isEmpty())
				return new UnacceptableAlternativeRank(owner(), project);

			return new PresentRankInPref(rankInArray.getAsInt());
		});
	}

	@Override
	public boolean isCompletelyIndifferent()
	{
		if (asList().isEmpty()) {
			return true;
		}

		return false;
	}

	@Override
	public Map<Project, RankInPref> asMap()
	{
		if (asMap == null) {
			// use default method and cache result
			asMap = ProjectPreference.super.asMap();
		}

		return asMap;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;

		// If not proj-pref -> not equal
		if (!(o instanceof ProjectPreference)) return false;

		// For now only allow comparison between this base-types (to be safe)
		// if/when exception is triggered, re-evaluate use-case
		if (!(o instanceof AbstractListBasedProjectPreferences)) throw new RuntimeException("Hmm AbsLBProjPref is being compared with some other type. Check if use-case is alright.");

		var that = (AbstractListBasedProjectPreferences) o;
		return this.asList().equals(that.asList());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.asList());
	}
}
