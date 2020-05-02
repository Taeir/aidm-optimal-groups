package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class WorstAssignedProjectRankOfStudents
{
	private final Matching<Group.FormedGroup, Project> matching;
	private Integer asInt = null;

	public WorstAssignedProjectRankOfStudents(Matching<Group.FormedGroup, Project> matching)
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
		var worst = AssignedProjectRankStudent.ranksOf(matching)
			.mapToInt(AssignedProjectRankStudent::studentsRank)
			.max().getAsInt();

		return worst;
	}
}
