package nl.tudelft.aidm.optimalgroups.dataset.bepsys.project;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.List;

public class ProjectsInDb extends ListBasedProjects.FromBackingList
{
	private CourseEdition courseEdition;

	public ProjectsInDb(List<Project> projects, CourseEdition courseEdition)
	{
		super(projects);
		this.courseEdition = courseEdition;
	}
}
