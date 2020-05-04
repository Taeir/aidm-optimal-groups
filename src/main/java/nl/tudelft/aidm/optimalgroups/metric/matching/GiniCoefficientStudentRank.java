package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
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
		int worstRank = new WorstAssignedProjectRankOfStudents(matching).asInt();

		var welfare = AssignedProjectRankStudent.ranksOf(matching)
			.map(AssignedProjectRankStudent::studentsRank)
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

	public Double asDouble()
	{
		return this.giniCoefficient;
	}

	public void printResult(PrintStream printStream)
	{
		printStream.printf("Gini-coef over student ranks: %f\n", asDouble());
	}
}
