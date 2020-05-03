package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.TopicGroupAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import plouchtch.assertion.Assert;

import java.util.ArrayList;
import java.util.List;
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

	static class MatchingPopularityComparison
	{
		final TopicGroupAlgorithm.Result a;
		final TopicGroupAlgorithm.Result b;

		final Integer numAgentsPreferingA;
		final Integer numAgentsPreferingB;

		public MatchingPopularityComparison(TopicGroupAlgorithm.Result a, TopicGroupAlgorithm.Result b)
		{
			this.a = a;
			this.b = b;

			var allAgents = a.result.datasetContext().allAgents();

			// Ranking of assigned project by Agents in matching A
			var agentToRankMapA = AssignedProjectRankStudent.ranksOf(a.result)
				.collect(Collectors.toMap(AssignedProjectRankStudent::student, AssignedProjectRankStudent::studentsRank));

			// Ranking of assigned project by Agents in matching B
			var agentToRankMapB = AssignedProjectRankStudent.ranksOf(b.result)
				.collect(Collectors.toMap(AssignedProjectRankStudent::student, AssignedProjectRankStudent::studentsRank));


			numAgentsPreferingA = (int) allAgents.asCollection().stream()
				.filter(agent -> agentToRankMapA.get(agent) > agentToRankMapB.get(agent))
				.count();

			numAgentsPreferingB = (int) allAgents.asCollection().stream()
				.filter(agent -> agentToRankMapB.get(agent) > agentToRankMapA.get(agent))
				.count();
		}
	}

}
