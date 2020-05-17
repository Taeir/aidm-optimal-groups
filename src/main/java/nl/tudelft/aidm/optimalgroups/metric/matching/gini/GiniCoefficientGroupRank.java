package nl.tudelft.aidm.optimalgroups.metric.matching.gini;

import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
import nl.tudelft.aidm.optimalgroups.metric.rank.WorstAssignedRank;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.io.PrintStream;
import java.util.stream.Collectors;

public class GiniCoefficientGroupRank implements GiniCoefficient
{
	private Match<Group.FormedGroup, Project> match;
	private final double giniCoefficient;

	public GiniCoefficientGroupRank(Matching<? extends Group, Project> matching)
	{
		int worstRank = new WorstAssignedRank.ProjectToGroup(matching).asInt();

		var welfare = AssignedRank.ProjectToGroup.groupRanks(matching)
			.map(AssignedRank.ProjectToGroup::asInt)
			.flatMap(optionalInt -> optionalInt.stream().boxed())
			.map(rank -> worstRank - rank +1)
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

	@Override
	public Double asDouble()
	{
		return this.giniCoefficient;
	}

	public void printResult(PrintStream printStream)
	{
		printStream.printf("Gini-coef over aggreg group rankings: %f\n", asDouble());
	}
}
