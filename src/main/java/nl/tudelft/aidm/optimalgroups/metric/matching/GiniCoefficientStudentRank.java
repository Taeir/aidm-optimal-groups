package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.io.PrintStream;
import java.util.stream.Collectors;

public class GiniCoefficientStudentRank
{
	private Match<Group.FormedGroup, Project> match;
	private final double giniCoefficient;

	public GiniCoefficientStudentRank(Matching<Group.FormedGroup, Project> matching)
	{
		int maxRank = matching.asList().get(0).from().members().asCollection().toArray(Agent[]::new)[0].projectPreference.asArray().length + 1;

		var ranks = matching.asList().stream()
			.flatMap(match -> match.from().members().asCollection().stream().map(member -> new AgentToProjectMatch(member, match.to())))
			.map(AssignedProjectRankStudent::new)
			.map(AssignedProjectRankStudent::studentsRank)
//			.map(rank -> maxRank - rank)
			.collect(Collectors.toList());

		var sumAbsDiff = ranks.stream().flatMap(i ->
				ranks.stream().map(j ->
					Math.abs(i - j)
			))
			.mapToInt(Integer::intValue) // for sum method of IntStream
			.sum();

		int n = ranks.size();
		int sum = ranks.stream().mapToInt(Integer::intValue).sum();
		this.giniCoefficient = sumAbsDiff / (2.0 * n * sum);
	}

	public Double asDouble()
	{
		return this.giniCoefficient;
	}

	public void printResult(PrintStream printStream)
	{
		printStream.printf("Gini-coef over student ranks: %f\n", asDouble());
	}
}
