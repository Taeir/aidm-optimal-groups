package nl.tudelft.aidm.optimalgroups.dataset.bepsys.project;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

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
}
