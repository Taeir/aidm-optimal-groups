package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.TopicGroupAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A matching is popular if there are more agents preferring it to the alternative.
 * Non-transitive: a,b,c in Algorithm then if: (a P b /\  b P c) does not imply a P c
 */
public class PopularityMatrix
{
	private List<TopicGroupAlgorithm.Result> algoResults;
	private List<MatchingPopularityComparison> algoPopularityComparisons;

	public PopularityMatrix(List<TopicGroupAlgorithm.Result> resultingMatchings)
	{
		this.algoResults = resultingMatchings;

		var allMatchingsBasedOnSameDatasetContext = resultingMatchings.stream()
			.map(algoResult -> algoResult.result.datasetContext())
			.distinct()
			.count() == 1;

		Assert.that(allMatchingsBasedOnSameDatasetContext).orThrow(RuntimeException.class,
			"Popularity metrix can only be determined for matchings based of same dataset context.");

		this.algoPopularityComparisons = new ArrayList<>();

		for (TopicGroupAlgorithm.Result result : resultingMatchings) {
			for (TopicGroupAlgorithm.Result otherResult : resultingMatchings) {

				if (result != otherResult) {
					algoPopularityComparisons.add(new MatchingPopularityComparison(result, otherResult));
				}

			}
		}
	}

	public Set<MatchingPopularityComparison> asSet()
	{
		return new HashSet<>(algoPopularityComparisons);
	}

	static class MatchingPopularityComparison
	{
		final TopicGroupAlgorithm.Result a;
		final TopicGroupAlgorithm.Result b;

		final int numAgentsPreferingA;
		final int numAgentsPreferingB;

		public MatchingPopularityComparison(TopicGroupAlgorithm.Result a, TopicGroupAlgorithm.Result b)
		{
			this.a = a;
			this.b = b;

			var allAgents = a.result.datasetContext().allAgents();

			// Ranking of assigned project by Agents in matching A
			var rankInMatchingA = AssignedProjectRankStudent.ranksOf(a.result)
				.collect(Collectors.toMap(AssignedProjectRankStudent::student, AssignedProjectRankStudent::studentsRank));

			// Ranking of assigned project by Agents in matching B
			var rankInMatchingB = AssignedProjectRankStudent.ranksOf(b.result)
				.collect(Collectors.toMap(AssignedProjectRankStudent::student, AssignedProjectRankStudent::studentsRank));


			numAgentsPreferingA = (int) allAgents.asCollection().stream()
				.filter(agent -> rankInMatchingA.get(agent) < rankInMatchingB.get(agent))
				.count();

			numAgentsPreferingB = (int) allAgents.asCollection().stream()
				.filter(agent -> rankInMatchingB.get(agent) < rankInMatchingA.get(agent))
				.count();
		}

		@Override
		public String toString()
		{
			String winner = "?";
			if (numAgentsPreferingA > numAgentsPreferingB) winner = " > ";
			else if (numAgentsPreferingA < numAgentsPreferingB) winner = " < ";
			else if (numAgentsPreferingA == numAgentsPreferingB) winner = " = ";

			return String.format("%s (%s) %s (%s) %s ", a.algo.name(), numAgentsPreferingA, winner, numAgentsPreferingB, b.algo.name());
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof MatchingPopularityComparison)) return false;
			MatchingPopularityComparison that = (MatchingPopularityComparison) o;
			return numAgentsPreferingA == that.numAgentsPreferingA &&
				numAgentsPreferingB == that.numAgentsPreferingB &&
				a.equals(that.a) &&
				b.equals(that.b);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(a, b, numAgentsPreferingA, numAgentsPreferingB);
		}
	}

}
