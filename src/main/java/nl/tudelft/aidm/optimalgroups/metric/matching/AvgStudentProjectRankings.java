package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class AvgStudentProjectRankings
{
	private final Matching<Group.FormedGroup, Project> matching;
	private Double asDouble = null;

	public AvgStudentProjectRankings(Matching<Group.FormedGroup, Project> matching)
	{
		this.matching = matching;
	}

	public Double asDouble()
	{
		if (asDouble == null) {
			asDouble = calculate();
		}

		return asDouble;
	}

	private Double calculate()
	{
		var sum = matching.asList().stream()
			.flatMap(match -> match.from().members().asCollection().stream().map(member -> new AgentToProjectMatch(member, match.to())))
			.map(AssignedProjectRankStudent::new)
			.mapToInt(AssignedProjectRankStudent::studentsRank)
			.sum() * 1.0;

		var numStudents = matching.asList().stream()
			.mapToInt(match -> match.from().members().count())
			.sum() * 1.0;

		return sum / numStudents;
	}
}
