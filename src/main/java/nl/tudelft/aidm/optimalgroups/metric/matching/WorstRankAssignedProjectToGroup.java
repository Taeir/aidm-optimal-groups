package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.bla.WorstRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankGroup;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class WorstRankAssignedProjectToGroup implements WorstRank
{
	private final Matching<? extends Group, Project> matching;
	private Integer asInt = null;

	public WorstRankAssignedProjectToGroup(Matching<? extends Group, Project> matching)
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
		var worst = AssignedProjectRankGroup.groupRanks(matching)
			.mapToInt(AssignedProjectRankGroup::groupRank)
			.max()
			.getAsInt();

		return worst;
	}
}
