package nl.tudelft.aidm.optimalgroups.metric.rank;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface AssignedRank
{
	/**
	 * An optional containing either the assigned rank, or is empty
	 * @return
	 */
	OptionalInt asInt();

	/**
	 * Indicates if the rank matters, that is if the agent is indifferent
	 * @return
	 */
	boolean isOfIndifferentAgent();

	class ProjectToGroup implements AssignedRank
	{
		private Match<? extends Group, Project> match;
		private OptionalInt rankAsInt = null;

		public ProjectToGroup(Match<? extends Group, Project> match)
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
				// Hmmm, AssignedRank existed before rankOf started returning RankInPref's
				var rank = match.from().projectPreference().rankOf(match.to());

				if (rank.unacceptable()) rankAsInt = OptionalInt.of(Integer.MAX_VALUE);
				else if (rank.isCompletelyIndifferent()) rankAsInt = OptionalInt.empty();
				else rankAsInt = OptionalInt.of(rank.asInt());
			}
		}

		public Collection<ProjectToStudent> studentRanks()
		{
			return match.from().members().asCollection().stream()
				.map(student -> new ProjectToStudent(student, match.to()))
				.collect(Collectors.toList());
		}

		public static Stream<ProjectToGroup> groupRanks(Matching<? extends Group, Project> matching)
		{
			return matching.asList().stream()
				.map(ProjectToGroup::new);
		}
	}

	class ProjectToStudent implements AssignedRank
	{
		private Agent student;
		private Project project;

		private OptionalInt rankAsInt = null;

		public ProjectToStudent(Match<Agent, Project> match)
		{
			this(match.from(), match.to());
		}

		public ProjectToStudent(Agent student, Project project)
		{
			this.student = student;
			this.project = project;
		}

		public Agent student()
		{
			return student;
		}

		@Override
		public OptionalInt asInt()
		{
			if (rankAsInt == null) {
				// Hmmm, AssignedRank existed before rankOf started returning RankInPref's
				var rank = student.projectPreference().rankOf(project);

				if (rank.unacceptable()) rankAsInt = OptionalInt.of(Integer.MAX_VALUE);
				else if (rank.isCompletelyIndifferent()) rankAsInt = OptionalInt.empty();
				else rankAsInt = OptionalInt.of(rank.asInt());
			}
			
			return rankAsInt;
		}

		@Override
		public boolean isOfIndifferentAgent()
		{
			return student.projectPreference().isCompletelyIndifferent();
		}

		public static Stream<ProjectToStudent> inGroupMatching(Matching<Group.FormedGroup, Project> matching)
		{
			return matching.asList().stream()
				.flatMap(match -> match.from().members().asCollection().stream().map(member -> new AgentToProjectMatch(member, match.to())))
				.map(ProjectToStudent::new);
		}

		public static Stream<ProjectToStudent> inStudentMatching(Matching<Agent, Project> matching)
		{
			return matching.asList().stream()
				.map(ProjectToStudent::new);
		}
	}
}
