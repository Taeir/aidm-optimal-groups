package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.Application;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.base.AbstractListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.PresentRankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankOfCompletelyIndifferentAgent;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.UnacceptableAlternativeRank;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;
import plouchtch.lang.Lazy;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * ProjectPreference implementation for a whole group. This is an average of the group member preferences (as implemented in BepSYS)
 */
public abstract class AggregatedProfilePreference extends AbstractListBasedProjectPreferences
{
	protected final Agents agents;

	protected Project[] asArray;
	protected List<Project> asList;
	protected Lazy<Map<Project, RankInPref>> asMap = new Lazy<>(this::calculateAverageOfGroup);

	protected DatasetContext datasetContext;

	public AggregatedProfilePreference(Agents agents)
	{
		this.agents = agents;
		this.datasetContext = agents.datasetContext;
	}

	protected abstract Map<Project, RankInPref> calculateAverageOfGroup();

	public Agents agentsAggregatedFrom()
	{
		return agents;
	}

	@Deprecated
	@Override
	public synchronized Project[] asArray()
	{
		if (asArray == null) {
			asArray = asList().toArray(Project[]::new);
		}

		return asArray;
	}

	@Deprecated
	@Override
	public synchronized List<Project> asList()
	{
		if (asList == null) {
			
			var asMap = this.asMap.get();
			asList = asMap.entrySet().stream()
					.filter(entry -> entry.getValue().isPresent())
					.sorted(Map.Entry.comparingByValue()) // RankInPref is a Comparable
					.map(Map.Entry::getKey) // Once ordered, extract the projects, tie-breaking is then quasi-random
					.toList();
		}

		return asList;
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
		return Arrays.equals(asArray, that.asArray);
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(asArray);
	}

	public static class Borda extends AggregatedProfilePreference
	{
		public Borda(Agents agents)
		{
			super(agents);
		}

		@Override
		public Object owner()
		{
			return agents;
		}

		@Override
		protected Map<Project, RankInPref> calculateAverageOfGroup()
		{
			// exception: handle fully indifferent groups
			if (agents.asCollection().stream().allMatch(agent -> agent.projectPreference().isCompletelyIndifferent())) {
				var map = new HashMap<Project, RankInPref>();
				var allProjects = agents.datasetContext.allProjects();
				allProjects.forEach(project -> map.put(project, new RankOfCompletelyIndifferentAgent(agents, project)));
				return map;
			}
			
			// mapping: Project --> score
			Map<Project, Integer> prefs = new LinkedHashMap<>();
			
			int maxRank = this.agents.datasetContext.worstRank().getAsInt() + 1;
			int unacceptableScore = maxRank + 1; // if project is unacceptible

			for (Agent agent : this.agents.asCollection()) {
				agent.projectPreference().forEach((project, rank) -> {
					int currentScoreSubtotal = prefs.getOrDefault(project, 0);
					
					var score = rank.unacceptable() ? unacceptableScore : rank.asInt();
					prefs.put(project, currentScoreSubtotal + rank.asInt());
					
					return true; // continue loop
				});
			}
			
			var groupedByScore = prefs.entrySet().stream()
					.collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toList())));
			
			// note: index-0 is rank 1
			var rankToScore = prefs.values().stream().sorted().toArray(Integer[]::new);
			
			var preferencesAsMap = new HashMap<Project, RankInPref>();
			
			for (int i = 0; i < rankToScore.length; i++)
			{
				var rank = i + 1;
				var score = rankToScore[i];
				var projects = groupedByScore.get(score);
				
				Assert.that(projects != null).orThrowMessage("BUGCHECK: no projects present at a certain score, but were expected");
				
				if (score == unacceptableScore * agents.count()) // unacceptible to all
					projects.forEach(project -> preferencesAsMap.put(project, new UnacceptableAlternativeRank(agents, project)));
				else
					projects.forEach(project -> preferencesAsMap.put(project, new PresentRankInPref(rank)));
			}
			
			return preferencesAsMap;
		}
	}

	public static class Copeland extends AggregatedProfilePreference
	{
		public Copeland(Agents agents)
		{
			super(agents);
		}

		@Override
		public Object owner()
		{
			return agents;
		}

		@Override
		protected Map<Project, RankInPref> calculateAverageOfGroup()
		{
			// exception: handle fully indifferent groups
			if (agents.asCollection().stream().allMatch(agent -> agent.projectPreference().isCompletelyIndifferent())) {
				var map = new HashMap<Project, RankInPref>();
				var allProjects = agents.datasetContext.allProjects();
				allProjects.forEach(project -> map.put(project, new RankOfCompletelyIndifferentAgent(agents, project)));
				return map;
			}
			
			final Set<Project> projects = new HashSet<>();

			// Retrieve the projects
			agents.forEach(agent -> {
				projects.addAll(agent.projectPreference().asList());
			});

			// Start comparing projects
			
			// Local comparison outcome types
			enum Outcome { Win, Lose, Tie }
			
			// Mapping of: Project x Project --> Win / Loss
			Map<Project, Map<Project, Outcome>> pairwiseComparison = new HashMap<>(projects.size());
			for (var currentProject : projects) {

				HashMap<Project, Outcome> currentComparison = new HashMap<>(projects.size());

				for (var otherProject : projects) {
					if (currentProject == otherProject)
						continue;

					int wins = 0;
					int defeats = 0;
					for (Agent a : this.agents.asCollection()) {
						var rankCurrent = a.projectPreference().rankOf(currentProject);
						var rankOther = a.projectPreference().rankOf(otherProject);
						
						var comp = rankCurrent.compareTo(rankOther);
						
						if (comp < 0) wins++;
						else if (comp > 0) defeats++;
					}

					if (wins > defeats) {
						currentComparison.put(otherProject, Outcome.Win);
					} else if (wins < defeats) {
						currentComparison.put(otherProject, Outcome.Lose);
					} else {
						currentComparison.put(otherProject, Outcome.Tie);
					}
				}

				pairwiseComparison.put(currentProject, currentComparison);
			}
		

			// Here, "score" is the amount of wins minus the amount of defeats
			Map<Project, Integer> projectScore = new HashMap<>();
			
			for (var entry : pairwiseComparison.entrySet()) {

				var project = entry.getKey();
				var comparisonResult = entry.getValue();

				// Start neutral
				projectScore.put(project, 0);
				
				for (var outcome : comparisonResult.values()) {
					switch (outcome) {
						case Win -> projectScore.merge(project, 1, Integer::sum);
						case Lose -> projectScore.merge(project, -1, Integer::sum);
					}
				}
			}
			
			// TODO: properly handle unacceptible alternatives -- for example, how to handle aggregation when one of the angents considers
			// the project unacceptible, but the others don't?
			
			var groupedByScore = projectScore.entrySet().stream()
					.collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toList())));
			
			// note: index-0 is rank 1
			var rankToScore = projectScore.values().stream().sorted().toArray(Integer[]::new);
			
			var preferencesAsMap = new HashMap<Project, RankInPref>();
			
			for (int i = 0; i < rankToScore.length; i++)
			{
				var rank = i + 1;
				var score = rankToScore[i];
				var projectsAtScore = groupedByScore.get(score);
				
				Assert.that(projects != null).orThrowMessage("BUGCHECK: no projects present at a certain score, but were expected");
			
				projectsAtScore.forEach(project -> preferencesAsMap.put(project, new PresentRankInPref(rank)));
			}
			
			// While we don't handle the unacceptible case, we do know that everything that's missing in the projectScores is unacceptible to all agents
			var unacceptibleToAllInvolved = datasetContext.allProjects().without(Projects.from(projects));
			unacceptibleToAllInvolved.forEach(unacceptibleProj -> preferencesAsMap.put(unacceptibleProj, new UnacceptableAlternativeRank(agents, unacceptibleProj)));
			
			return preferencesAsMap;
		}
	}
}
