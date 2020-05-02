package nl.tudelft.aidm.optimalgroups.dataset.bepsys;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.project.ProjectsInDb;
import nl.tudelft.aidm.optimalgroups.support.Hostname;
import org.sql2o.GenericDatasource;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CourseEdition implements DatasetContext
{
	private final static Map<Integer, CourseEdition> cachedCourseEditions = new HashMap<>();

	private final DataSource dataSource;

	private final int courseEditionId;
	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;


	private CourseEdition(DataSource dataSource, int courseEditionId)
	{
		this.dataSource = dataSource;
		this.courseEditionId = courseEditionId;

		this.groupSizeConstraint = new GroupSizeConstraintBepSys(dataSource, this);

		this.agents = fetchAgents(dataSource, this);
		this.projects = fetchProjects(dataSource, this);
	}

	public static CourseEdition fromLocalBepSysDbSnapshot(int courseEditionId)
	{
		return CourseEdition.fromBepSysDatabase(dataSourceToLocalDb(), courseEditionId);
	}

	public static CourseEdition fromBepSysDatabase(DataSource dataSource, int courseEditionId)
	{
		CourseEdition courseEdition = cachedCourseEditions.get(courseEditionId);
		if (courseEdition == null)
		{
			courseEdition = new CourseEdition(dataSource, courseEditionId);
			cachedCourseEditions.put(courseEditionId, courseEdition);
		}

		return courseEdition;
	}

	public Integer bepSysId()
	{
		return courseEditionId;
	}

	@Override
	public String identifier()
	{
		return String.format("BepSys%s%s", courseEditionId, groupSizeConstraint);
	}

	@Override
	public Projects allProjects()
	{
		return projects;
	}

	@Override
	public Agents allAgents()
	{
		return agents;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CourseEdition that = (CourseEdition) o;
		return identifier().equals(that.identifier());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(identifier());
	}

	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		return groupSizeConstraint;
	}


	private static Agents fetchAgents(DataSource dataSource, CourseEdition courseEdition)
	{
		var sql2o = new Sql2o(dataSource);
		try (var connection = sql2o.open())
		{
			var query = connection.createQuery("SELECT distinct user_id as user_id " +
				"FROM course_participations " +
				"WHERE course_edition_id = :courseEditionId"
			)
				.addParameter("courseEditionId", courseEdition.bepSysId());

			List<Integer> userIds = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("user_id"));
			List<Agent> agents = userIds.stream()
				.map(id -> new Agent.AgentInBepSysSchemaDb(dataSource, id, courseEdition))
				.collect(Collectors.toList());

			return Agents.from(agents);
		}
	}

	private static Projects fetchProjects(DataSource dataSource, CourseEdition courseEdition)
	{
		var sql = "SELECT id FROM projects where course_edition_id = :courseEditionId";
		try (var connection = new Sql2o(dataSource).open())
		{
			Query query = connection.createQuery(sql);
			query.addParameter("courseEditionId", courseEdition.courseEditionId);

			List<Project> projectsAsList = query.executeAndFetch(
				(ResultSetHandler<Project>) rs ->
					new Project.ProjectsWithDefaultSlotAmount(rs.getInt("id"))
			);

			return new ProjectsInDb(projectsAsList, courseEdition);
		}
	}

	private static DataSource dataSourceToLocalDb()
	{
		switch (Hostname.ofThisMachine().toString())
		{
			case "COOLICER-DESK":
				return new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
			case "PHILIPE-LAPTOP":
				return new GenericDatasource("jdbc:mysql://localhost:3306/test", "henk", "henk");
			default:
				throw new RuntimeException("Unknown machine, don't know connection string to DB");
		}
	}
}
