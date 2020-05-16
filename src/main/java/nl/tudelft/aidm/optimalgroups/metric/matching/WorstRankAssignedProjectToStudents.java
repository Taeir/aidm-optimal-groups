package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.bla.WorstRank;
import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.OptionalInt;

public class WorstRankAssignedProjectToStudents implements WorstRank
{
	private final Matching<Agent, Project> matching;
	private Integer asInt = null;

	public WorstRankAssignedProjectToStudents(Matching<Agent, Project> matching)
	{
		this.matching = matching;
	}

	public Integer asInt()
	{
		if (asInt == null) {
			asInt = calculate();
		}

		return asInt;
	}

	private Integer calculate()
	{
		var worst = AssignedProjectRankStudent.inStudentMatching(matching)
			.map(AssignedProjectRankStudent::asInt)
			.filter(OptionalInt::isPresent)
			.mapToInt(OptionalInt::getAsInt)
			.max().orElseThrow();

		return worst;
	}
}
