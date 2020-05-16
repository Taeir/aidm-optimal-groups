package nl.tudelft.aidm.optimalgroups.metric.rank;

import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssignedProjectRankGroup implements AssignedRank
{
	private Match<? extends Group, Project> match;
	private OptionalInt rankAsInt = null;

	public AssignedProjectRankGroup(Match<? extends Group, Project> match)
	{
		this.match = match;
	}

	/**
	 * {@inheritDoc}
	 * The rank of the assigned project in the group preference list
	 * @return The rank [1...N] where 1 is most preferred
	 * @throws NoSuchElementException if no rank can be meaningfully determined
	 */
	@Override
	public OptionalInt asInt()
	{
		determine();
		return rankAsInt;
	}

	/**
	 * {@inheritDoc}
	 * @return True if all agents in group are indifferent
	 */
	@Override
	public boolean isOfIndifferentAgent()
	{
		determine();
		return match.from().members().asCollection().stream().allMatch(agent -> agent.projectPreference().isCompletelyIndifferent());
	}

	private void determine()
	{
		if (rankAsInt == null) {
			int projectId = match.to().id();
			Integer[] preferences = match.from().projectPreference().asArray();

			RankInArray rankInArray = new RankInArray();
			rankAsInt = rankInArray.determineRank(projectId, preferences);
		}
	}

	public Collection<AssignedProjectRankStudent> studentRanks()
	{
		return match.from().members().asCollection().stream()
			.map(student -> new AssignedProjectRankStudent(student, match.to()))
			.collect(Collectors.toList());
	}

	public static Stream<AssignedProjectRankGroup> groupRanks(Matching<? extends Group, Project> matching)
	{
		return matching.asList().stream()
			.map(AssignedProjectRankGroup::new);
	}
}
