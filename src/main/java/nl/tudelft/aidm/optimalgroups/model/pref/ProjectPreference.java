package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.metric.rank.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankOfCompletelyIndifferentAgent;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.PresentRankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.UnacceptableAlternativeRank;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.*;

/**
 * Note: currently these are total preferences (no ties) - but not necessarily complete. Only missing alternatives can be partially ordered (tied)
 */
public interface ProjectPreference
{
	Project[] asArray();
	List<Project> asList();

	/**
	 * Whose preferences are these?
	 * @return the owner
	 */
	Object owner();
	
	/**
	 * Iterate over this project preference in order from most to least desired
	 * @param iter The function/consumer taking in a project and rank - returning false to break iteration
	 */
	default void forEach(ProjecPrefIterFn iter)
	{
		List<Project> projectList = asList();
		for (Project proj : projectList) {
			var rank = rankOf(proj);
			var continu = iter.apply(proj, rank);
			
			if (!continu) return;
		}
	}

	/**
	 * Checks if the project preferences indicate complete indifference, that is an absence of preference.
	 * In case of BepSys: the agent has no preferences available. In other scenarios this might mean that the
	 * the available choices have equal rank to the agent
	 * @return
	 */
	default boolean isCompletelyIndifferent()
	{
		return asList().size() == 0;
	}
	
	/**
	 * Indicates the rank of the given project in preferences.
	 * TODO: Proper handling of acceptible/indifferent/ties - currently, a project is deemed unaccptable if it is missing from the array/list
	 * @param project
	 * @return
	 */
	default RankInPref rankOf(Project project)
	{
		if (isCompletelyIndifferent()) {
			return new RankOfCompletelyIndifferentAgent(owner(), project);
		}

		var inArray = new RankInArray().determineRank(project, this.asArray());
		if (inArray.isEmpty()) {
			return new UnacceptableAlternativeRank(owner(), project);
		}

		return new PresentRankInPref(inArray.getAsInt());
	}
	
	/**
	 * Returns the worst rank in the prefs
	 * @return Empty if indifferent
	 */
	default OptionalInt maxRank()
	{
		if (isCompletelyIndifferent()) {
			return OptionalInt.empty();
		}
		
		return asMap().values().stream()
				   .filter(RankInPref::isPresent)
				   .mapToInt(RankInPref::asInt)
			       .max();
	}

	/**
	 * Return the preferences as a map, where the keys represent the project
	 * and the value represents the rank of the project.
	 *
	 * The highest rank is 1 and represents the most preferable project.
	 *
	 * @return Map
	 */
	default Map<Project, RankInPref> asMap()
	{
		var preferencesMap = new HashMap<Project, RankInPref>(this.asList().size());
		
		for (Project project : asList())
		{
			var rank = rankOf(project);
			preferencesMap.put(project, rank);
		}

		return preferencesMap;
	}
	
	default int differenceTo(ProjectPreference otherPreference)
	{
		var own = asMap();
		var other = otherPreference.asMap();

		// If the other does not have any preferences, return maximum difference to
		// avoid picking this matchings over people that do have preferences
		if (other.size() == 0 || own.size() == 0) {
			return Integer.MAX_VALUE;
		}

		int difference = 0;
		for (var entry : own.entrySet()) {
			var ownRank = entry.getValue();
			var othersRank = other.get(entry.getKey());
			
			
			if (ownRank.isPresent() && othersRank.isPresent()) {
				var rankOwn = entry.getValue().asInt();
				var rankOthers = other.get(entry.getKey()).asInt();
				
				difference += Math.abs(rankOwn - rankOthers);
			}
			// exactly one of us finds the project unacceptible
			else if (ownRank.unacceptable() ^ othersRank.unacceptable()) {
				var presentRank = ownRank.isPresent() ? ownRank.asInt() : othersRank.asInt();
				
				difference += (Math.max(own.size(), other.size()) + 1) - presentRank;
			}
		}

		return difference;
	}

	interface ProjecPrefIterFn
	{
		/**
		 * @param project The project with the given rank
		 * @param rank Rank in preference, 1 being highest - empty if indifferent
		 * @return true if iteration should continue
		 */
		boolean apply(Project project, RankInPref rank);
	}

}
