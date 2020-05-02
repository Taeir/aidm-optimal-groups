package nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned;

import nl.tudelft.aidm.optimalgroups.metric.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.stream.Stream;

public class AssignedProjectRankStudent
{
	private Agent student;
	private Project projectSlot;

	public AssignedProjectRankStudent(AgentToProjectMatch match)
	{
		this(match.from(), match.to());
	}

	public AssignedProjectRankStudent(Agent student, Project projectSlot)
	{
		this.student = student;
		this.projectSlot = projectSlot;
	}

	public Agent student()
	{
		return student;
	}

	public int studentsRank()
	{
		int projectId = projectSlot.id();

		if (student.projectPreference.isCompletelyIndifferent())
			return -1;

		int rank = new RankInArray().determineRank(projectId, student.projectPreference.asArray());
		return rank;
	}

	public static Stream<AssignedProjectRankStudent> ranksOf(Matching<Group.FormedGroup, Project> matching)
	{
		return matching.asList().stream()
			.flatMap(match -> match.from().members().asCollection().stream().map(member -> new AgentToProjectMatch(member, match.to())))
			.map(AssignedProjectRankStudent::new);
	}
}
