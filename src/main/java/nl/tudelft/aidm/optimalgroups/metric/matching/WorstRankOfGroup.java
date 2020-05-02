package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class WorstRankOfGroup
{
	private final Matching<Group.FormedGroup, Project> matching;
	private Integer asInt = null;

	public WorstRankOfGroup(Matching<Group.FormedGroup, Project> matching)
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
		var worst = matching.asList().stream()
			.mapToInt(formedGroupProjectMatch -> formedGroupProjectMatch.from().projectPreference().rankOf(formedGroupProjectMatch.to()))
			.max()
			.getAsInt();

		return worst;
	}
}
