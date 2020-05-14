package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.Algorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.AgentProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A matching is popular if there are more agents preferring it to the alternative.
 * Non-transitive: a,b,c in Algorithm then if: (a P b /\  b P c) does not imply a P c
 */
public class PopularityMatrix<MATCHING extends Matching, ALGO extends Algorithm, ALGORES extends Algorithm.Result<ALGO, MATCHING>>
{
	public static class TopicGroup extends PopularityMatrix<GroupToProjectMatching<Group.FormedGroup>, GroupProjectAlgorithm, GroupProjectAlgorithm.Result>
	{
		public TopicGroup(List<? extends GroupProjectAlgorithm.Result> resultingMatchings)
		{
			super(resultingMatchings, MatchingPopularityComparisonTopicGroup::new);
		}

		static class MatchingPopularityComparisonTopicGroup extends MatchingPopularityComparison<GroupProjectAlgorithm.Result>
		{
			final int numAgentsPreferingA;
			final int numAgentsPreferingB;

			public MatchingPopularityComparisonTopicGroup(GroupProjectAlgorithm.Result a, GroupProjectAlgorithm.Result b)
			{
				super(a, b);

				var allAgents = a.result().datasetContext().allAgents();

				// Ranking of assigned project by Agents in matching A
				var rankInMatchingA = AssignedProjectRankStudent.inGroupMatching(a.result())
					.collect(Collectors.toMap(AssignedProjectRankStudent::student, AssignedProjectRankStudent::asInt));

				// Ranking of assigned project by Agents in matching B
				var rankInMatchingB = AssignedProjectRankStudent.inGroupMatching(b.result())
					.collect(Collectors.toMap(AssignedProjectRankStudent::student, AssignedProjectRankStudent::asInt));


				numAgentsPreferingA = (int) allAgents.asCollection().stream()
					.filter(agent -> rankInMatchingA.get(agent) < rankInMatchingB.get(agent))
					.count();

				numAgentsPreferingB = (int) allAgents.asCollection().stream()
					.filter(agent -> rankInMatchingB.get(agent) < rankInMatchingA.get(agent))
					.count();
			}
		}
	}

	public static class StudentProject extends PopularityMatrix<AgentToProjectMatching, AgentProjectAlgorithm, AgentProjectAlgorithm.Result>
	{
		public StudentProject(List<? extends AgentProjectAlgorithm.Result> resultingMatchings)
		{
			super(resultingMatchings, MatchingPopularityComparisonStudentTopic::new);
		}

		static class MatchingPopularityComparisonStudentTopic extends MatchingPopularityComparison<AgentProjectAlgorithm.Result>
		{
			final int numAgentsPreferingA;
			final int numAgentsPreferingB;

			public MatchingPopularityComparisonStudentTopic(AgentProjectAlgorithm.Result a, AgentProjectAlgorithm.Result b)
			{
				super(a, b);

				var allAgents = a.result().datasetContext().allAgents();

				// Ranking of assigned project by Agents in matching A
				var rankInMatchingA = AssignedProjectRankStudent.inStudentMatching(a.result())
					.collect(Collectors.toMap(AssignedProjectRankStudent::student, AssignedProjectRankStudent::asInt));

				// Ranking of assigned project by Agents in matching B
				var rankInMatchingB = AssignedProjectRankStudent.inStudentMatching(b.result())
					.collect(Collectors.toMap(AssignedProjectRankStudent::student, AssignedProjectRankStudent::asInt));


				numAgentsPreferingA = (int) allAgents.asCollection().stream()
					.filter(agent -> rankInMatchingA.get(agent) < rankInMatchingB.get(agent))
					.count();

				numAgentsPreferingB = (int) allAgents.asCollection().stream()
					.filter(agent -> rankInMatchingB.get(agent) < rankInMatchingA.get(agent))
					.count();
			}
		}
	}

	/* POPULARITY MATRIX IMPL BELOW */
	private List<? extends ALGORES> algoResults;
	private List<MatchingPopularityComparison<ALGORES>> algoPopularityComparisons;

	public PopularityMatrix(List<? extends ALGORES> resultingMatchings, BiFunction<ALGORES, ALGORES, MatchingPopularityComparison<ALGORES>> compare)
	{
		this.algoResults = resultingMatchings;

		var allMatchingsBasedOnSameDatasetContext = resultingMatchings.stream()
			.map(algoResult -> algoResult.result().datasetContext())
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

	static abstract class MatchingPopularityComparison<ALGORES extends Algorithm.Result>
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
			return numAgentsPreferingA == that.numAgentsPreferingA &&
				numAgentsPreferingB == that.numAgentsPreferingB &&
				resultA.equals(that.resultA) &&
				resultB.equals(that.resultB);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(resultA, resultB, numAgentsPreferingA, numAgentsPreferingB);
		}
	}


}
