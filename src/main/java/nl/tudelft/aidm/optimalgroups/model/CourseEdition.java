package nl.tudelft.aidm.optimalgroups.model;

import nl.tudelft.aidm.optimalgroups.model.agent.Agents;

import javax.sql.DataSource;

public class CourseEdition
{
	public final int id;
	public final Agents agents;
	public final Projects projects;
	public final GroupSizeConstraint groupSizeConstraint;

	private CourseEdition(int id, Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		this.id = id;
		this.agents = agents;
		this.projects = projects;
		this.groupSizeConstraint = groupSizeConstraint;
	}

	public static CourseEdition fromBepSysDatabase(DataSource dataSource, int courseEditionId)
	{
		Agents agents = Agents.fromBepSysDb(dataSource, courseEditionId);
		Projects projects = Projects.fromDb(dataSource, courseEditionId);
		GroupSizeConstraint groupSizeConstraint = new GroupSizeConstraint.fromDb(dataSource, courseEditionId);

		return new CourseEdition(courseEditionId, agents, projects, groupSizeConstraint);
	}
}
