package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

import java.util.Collection;
import java.util.stream.Collectors;

public class AssignedProjectRankGroup
{
	private Matching.Match<? extends Group, Project> match;

	public AssignedProjectRankGroup(Matching.Match<? extends Group, Project> match)
	{
		this.match = match;
	}

	/**
	 * The rank of the assigned project in the group preference list
	 * @return The rank [1...N] where 1 is most preferred
	 */
	public int groupRank()
	{
		int projectId = match.to().id();
		int[] preferences = match.from().projectPreference().asArray();

		RankInArray rankInArray = new RankInArray();
		int rankNumber = rankInArray.determineRank(projectId, preferences);

		return rankNumber;
	}

	public Collection<AssignedProjectRankStudent> studentRanks()
	{
		return match.from().members().asCollection().stream()
			.map(student -> new AssignedProjectRankStudent(student, match.to()))
			.collect(Collectors.toList());
	}
}
