package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.bla.AvgRank;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankGroup;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class AvgRankAssignedProjectToGroup implements AvgRank
{
	private final Matching<? extends Group, Project> matching;
	private Double asDouble = null;

	public AvgRankAssignedProjectToGroup(Matching<? extends Group, Project> matching)
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
			.map(AssignedProjectRankGroup::new)
			.mapToInt(AssignedProjectRankGroup::groupRank)
			.sum() * 1.0;

		var numStudents = matching.asList().size();

		return sum / numStudents;
	}
}
