package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankGroup;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class WorstAssignedProjectRankOfGroups
{
	private final Matching<Group.FormedGroup, Project> matching;
	private Integer asInt = null;

	public WorstAssignedProjectRankOfGroups(Matching<Group.FormedGroup, Project> matching)
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
		var worst = AssignedProjectRankGroup.ranksOf(matching)
			.mapToInt(AssignedProjectRankGroup::groupRank)
			.max()
			.getAsInt();

		return worst;
	}
}
