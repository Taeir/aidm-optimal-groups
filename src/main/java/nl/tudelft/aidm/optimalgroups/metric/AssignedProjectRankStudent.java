package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

public class AssignedProjectRankStudent
{
	private Agent student;
	private Project.ProjectSlot projectSlot;

	public AssignedProjectRankStudent(Agent student, Project.ProjectSlot projectSlot)
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
		int projectId = projectSlot.belongingToProject().id();

		int rank = new RankInArray().determineRank(projectId, student.projectPreference.asArray());
		return rank;
	}
}
