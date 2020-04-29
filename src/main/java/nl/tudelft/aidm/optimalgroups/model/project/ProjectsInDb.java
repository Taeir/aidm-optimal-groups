package nl.tudelft.aidm.optimalgroups.model.project;

import nl.tudelft.aidm.optimalgroups.model.dataset.CourseEdition;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import plouchtch.assertion.Assert;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProjectsInDb extends ListBasedProjects
{
	private CourseEdition courseEdition;

	private List<Project> projectList;
	private Map<Integer, Project> byId = null;

	public ProjectsInDb(List<Project> projects, CourseEdition courseEdition)
	{
		this.projectList = projects;
		this.courseEdition = courseEdition;
	}

	@Override
	protected List<Project> projectList()
	{
		return projectList;
	}

	@Override
	public synchronized Optional<Project> findWithId(int projectId)
	{
		if (byId == null) {
			byId = new HashMap<>();
			projectList().forEach(project -> {
				byId.put(project.id(), project);
			});
		}

		Project value = byId.get(projectId);
		return Optional.ofNullable(value);
	}

}
