package nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned;

import nl.tudelft.aidm.optimalgroups.metric.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.stream.Stream;

public class AssignedProjectRankStudent
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

	public int asInt()
	{
		int projectId = project.id();

		if (student.projectPreference.isCompletelyIndifferent())
			return -1;

		int rank = new RankInArray().determineRank(projectId, student.projectPreference.asArray());
		return rank;
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
