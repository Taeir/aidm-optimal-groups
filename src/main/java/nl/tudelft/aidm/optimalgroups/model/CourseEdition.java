package nl.tudelft.aidm.optimalgroups.model;

import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

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

	private static Map<Integer, CourseEdition> cachedCourseEditions = new HashMap<>();

	public static CourseEdition fromBepSysDatabase(DataSource dataSource, int courseEditionId)
	{
		CourseEdition cached = cachedCourseEditions.get(courseEditionId);
		if (cached != null) {
			return cached;
		}

		Agents agents = Agents.fromBepSysDb(dataSource, courseEditionId);
		Projects projects = Projects.fromDb(dataSource, courseEditionId);
		GroupSizeConstraint groupSizeConstraint = new GroupSizeConstraint.fromDb(dataSource, courseEditionId);

		CourseEdition courseEdition = new CourseEdition(courseEditionId, agents, projects, groupSizeConstraint);
		cachedCourseEditions.put(courseEditionId, courseEdition);

		return courseEdition;
	}
}
