package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.bla.AvgRank;
import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.OptionalInt;

public class AvgRankAssignedProjectToStudent implements AvgRank
{
	private final Matching<Agent, Project> matching;
	private Double asDouble = null;

	public AvgRankAssignedProjectToStudent(Matching<Agent, Project> matching)
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
			.map(AssignedProjectRankStudent::new)
			.map(AssignedProjectRankStudent::asInt).flatMapToInt(OptionalInt::stream)
			.sum() * 1.0;

		var numStudents = matching.asList().size();

		return sum / numStudents;
	}
}
