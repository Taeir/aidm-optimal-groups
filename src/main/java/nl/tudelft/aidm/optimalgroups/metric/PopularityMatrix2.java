package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.Algorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
import nl.tudelft.aidm.optimalgroups.model.HasProjectPrefs;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A matching is popular if there are more agents preferring it to the alternative.
 * Non-transitive: a,b,c in Algorithm then if: (a P b /\  b P c) does not imply a P c
 */
public class PopularityMatrix2<F extends HasProjectPrefs, MATCHING extends Matching<F,Project>, ALGO extends Algorithm, ALGORES extends Algorithm.Result<ALGO, MATCHING>>
{
	public static class TopicGroup extends PopularityMatrix2<Group.FormedGroup, GroupToProjectMatching<Group.FormedGroup>, GroupProjectAlgorithm, GroupProjectAlgorithm.Result>
	{
		public TopicGroup(List<? extends GroupProjectAlgorithm.Result> resultingMatchings)
		{
			super(resultingMatchings, MatchingPopularityComparisonTopicGroup::new);
		}

		static class MatchingPopularityComparisonTopicGroup extends MatchingPopularityComparison<GroupProjectAlgorithm.Result>
		{
			public MatchingPopularityComparisonTopicGroup(GroupProjectAlgorithm.Result a, GroupProjectAlgorithm.Result b)
			{
				super(a, b);
				
				Assert.that(a.producedMatching().datasetContext().equals(b.producedMatching().datasetContext()))
						.orThrowMessage("DatasetContext mismatch");

				var allAgents = a.producedMatching().datasetContext().allAgents();

				// Ranking of assigned project by Agents in matching A
				var rankInMatchingA = AssignedRank.ProjectToStudent.inGroupMatching(a.producedMatching())
					.collect(Collectors.toMap(AssignedRank.ProjectToStudent::student, AssignedRank.ProjectToStudent::asInt));

				// Ranking of assigned project by Agents in matching B
				var rankInMatchingB = AssignedRank.ProjectToStudent.inGroupMatching(b.producedMatching())
					.collect(Collectors.toMap(AssignedRank.ProjectToStudent::student, AssignedRank.ProjectToStudent::asInt));


				numAgentsPreferingA = (int) allAgents.asCollection().stream()
					.filter(agent -> {
						int rankInA = rankInMatchingA.getOrDefault(agent, OptionalInt.empty()).orElse(Integer.MAX_VALUE);
						int rankInB = rankInMatchingB.getOrDefault(agent, OptionalInt.empty()).orElse(Integer.MAX_VALUE);
						// All agents who prefer matching A to B (rank the assigned project in matching A higher than in B)
						return rankInA < rankInB;
					})
					.count();

				numAgentsPreferingB = (int) allAgents.asCollection().stream()
					.filter(agent -> {
						int rankInB = rankInMatchingB.getOrDefault(agent, OptionalInt.empty()).orElse(Integer.MAX_VALUE);
						int rankInA = rankInMatchingA.getOrDefault(agent, OptionalInt.empty()).orElse(Integer.MAX_VALUE);
						return rankInB < rankInA;
					})
					.count();
			}
		}
	}

	/* POPULARITY MATRIX IMPL BELOW */
	private List<? extends ALGORES> algoResults;
	private List<MatchingPopularityComparison<ALGORES>> algoPopularityComparisons;

	public PopularityMatrix2(List<? extends ALGORES> resultingMatchings, BiFunction<ALGORES, ALGORES, MatchingPopularityComparison<ALGORES>> compare)
	{
		this.algoResults = resultingMatchings;

		var allMatchingsBasedOnSameDatasetContext = resultingMatchings.stream()
			.map(algoResult -> algoResult.producedMatching().datasetContext())
			.distinct()
			.count() == 1;

		Assert.that(allMatchingsBasedOnSameDatasetContext).orThrow(RuntimeException.class,
			"Popularity metrix can only be determined for matchings based of same dataset context.");

		this.algoPopularityComparisons = new ArrayList<>();

		for (var result : resultingMatchings) {
			for (var otherResult : resultingMatchings) {

				if (result != otherResult) {
					var comparison = compare.apply(result, otherResult);
					algoPopularityComparisons.add(comparison);
				}

			}
		}
	}

	public Set<MatchingPopularityComparison<ALGORES>> asSet()
	{
		return new HashSet<>(algoPopularityComparisons);
	}
	
	public List<MatchingPopularityComparison<ALGORES>> asList()
	{
		return Collections.unmodifiableList(algoPopularityComparisons);
	}
	
	public List<MatchingPopularityComparison<ALGORES>> deduplicatedByWinner()
	{
		var asSet = this.asSet();
		
		var wins = asSet.stream().map(MatchingPopularityComparison::winner)
				.flatMap(Optional::stream)
				.collect(Collectors.groupingBy(a -> a, Collectors.counting()));
		
		var algos = asList().stream()
				.mapMulti((MatchingPopularityComparison<ALGORES> compResult, Consumer<Algorithm> consumer) -> {
					consumer.accept(compResult.resultA.algo());
					consumer.accept(compResult.resultB.algo());
				})
				.distinct()
				.toList();
		
		var results = new HashMap<Algorithm, List<MatchingPopularityComparison<ALGORES>>>();
		for (Algorithm algo : algos)
		{
			var winningOrTiedComps = asList().stream()
					.filter(comp -> comp.resultA.algo().equals(algo) && comp.winner().orElse(algo).equals(algo))
					.toList();
			
			results.put(algo, winningOrTiedComps);
		}
		
		return algos.stream().sorted((Comparator.comparing(a -> wins.getOrDefault(a, 0L))).reversed())
				.mapMulti((Algorithm algorithm, Consumer<MatchingPopularityComparison<ALGORES>> putIntoStream) -> {
					results.get(algorithm).forEach(putIntoStream);
				})
				.toList();
	}

	public static abstract class MatchingPopularityComparison<ALGORES extends Algorithm.Result>
	{
		private final ALGORES resultA;
		private final ALGORES resultB;

		int numAgentsPreferingA;
		int numAgentsPreferingB;

		public MatchingPopularityComparison(ALGORES resultA, ALGORES resultB)
		{
			this.resultA = resultA;
			this.resultB = resultB;
		}
		
		/**
		 * @return The winning algorithm of this comparison, empty if tie
		 */
		public Optional<Algorithm> winner()
		{
			if (numAgentsPreferingA > numAgentsPreferingB) return Optional.of(resultA.algo());
			if (numAgentsPreferingB > numAgentsPreferingA) return Optional.of(resultB.algo());
			return Optional.empty();
		}

		@Override
		public String toString()
		{
			String winner = "?";
			if (numAgentsPreferingA > numAgentsPreferingB) winner = " > ";
			else if (numAgentsPreferingA < numAgentsPreferingB) winner = " < ";
			else if (numAgentsPreferingA == numAgentsPreferingB) winner = " = ";

			return String.format("%s (%s) %s (%s) %s ", resultA.algo().name(), numAgentsPreferingA, winner, numAgentsPreferingB, resultB.algo().name());
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof MatchingPopularityComparison)) return false;
			MatchingPopularityComparison<?> that = (MatchingPopularityComparison<?>) o;
			
			// Algo are same in both comparisons
			if (this.resultA.algo() == that.resultA.algo() && this.resultB.algo() == that.resultB.algo())
			{
				Assert.that(this.numAgentsPreferingA == that.numAgentsPreferingA && this.numAgentsPreferingB == that.numAgentsPreferingB).orThrowMessage("Algos match, results do not");
				return true;
			}
			
			// The inverse case, this A is that B
			if (this.resultA.algo() == that.resultB.algo() && this.resultB.algo() == that.resultA.algo())
			{
				Assert.that(this.numAgentsPreferingA == that.numAgentsPreferingB && this.numAgentsPreferingB == that.numAgentsPreferingA).orThrowMessage("Algos match, results do not");
				return true;
			}
			
			return false;
		}

		@Override
		public int hashCode()
		{
			// Ensure hashCode is invarant over the order of params
			// (comparison(a, b).equals( comparison(b,a) ) implies comparison(a,b).hashCode == comaprison(b,a).hashCode
			
			if (numAgentsPreferingA >= numAgentsPreferingB)
				return Objects.hash(resultA.algo(), resultB.algo());
			
			return Objects.hash(resultB.algo(), resultA.algo());
		}
	}


}
