package nl.tudelft.aidm.optimalgroups.metric.rank;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.OptionalInt;
import java.util.stream.Stream;

public class AssignedProjectRankStudent implements AssignedRank
{
	private Agent student;
	private Project project;

	public AssignedProjectRankStudent(Match<Agent, Project> match)
	{
		this(match.from(), match.to());
	}

	public AssignedProjectRankStudent(Agent student, Project project)
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
		int projectId = project.id();
		Integer[] preferencesAsArray = student.projectPreference().asArray();

		return new RankInArray().determineRank(projectId, preferencesAsArray);
	}

	@Override
	public boolean isOfIndifferentAgent()
	{
		return student.projectPreference().isCompletelyIndifferent();
	}

	public static Stream<AssignedProjectRankStudent> inGroupMatching(Matching<Group.FormedGroup, Project> matching)
	{
		return matching.asList().stream()
			.flatMap(match -> match.from().members().asCollection().stream().map(member -> new AgentToProjectMatch(member, match.to())))
			.map(AssignedProjectRankStudent::new);
	}

	public static Stream<AssignedProjectRankStudent> inStudentMatching(Matching<Agent, Project> matching)
	{
		return matching.asList().stream()
			.map(AssignedProjectRankStudent::new);
	}
}
