package nl.tudelft.aidm.optimalgroups.metric.matching.gini;

import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
import nl.tudelft.aidm.optimalgroups.metric.rank.WorstAssignedRank;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.io.PrintStream;
import java.util.stream.Collectors;

public class GiniCoefficientStudentRank implements GiniCoefficient
{
	private final double giniCoefficient;

	public GiniCoefficientStudentRank(Matching<Agent, Project> matching)
	{
		int worstRank = new WorstAssignedRank.ProjectToStudents(matching).asInt();

		var welfare = matching.asList().stream()
				.map(match -> match.from().projectPreference().rankOf(match.to()))
				.filter(rank -> rank.isPresent() || rank.unacceptable())
				// flip rank to income (good rank -> higher income) and handle unacceptibles by assigned them income = 0
				.map(rank -> rank.unacceptable() ? 0 : worstRank - rank.asInt() + 1)
				.collect(Collectors.toList());

		var sumAbsDiff = welfare.stream().flatMap(i ->
				welfare.stream().map(j ->
					Math.abs(i - j)
			))
			.mapToInt(Integer::intValue) // for sum method of IntStream
			.sum();

		int n = welfare.size();
		int sum = welfare.stream().mapToInt(Integer::intValue).sum();
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
