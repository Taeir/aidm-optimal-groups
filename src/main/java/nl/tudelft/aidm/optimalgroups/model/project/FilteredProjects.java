package nl.tudelft.aidm.optimalgroups.model.project;

import java.util.List;
import java.util.stream.Collectors;

public class FilteredProjects extends ListBasedProjects
{
	private final List<Project> projects;
	private final Project excluded;

	public FilteredProjects(List<Project> projects, Project excluded)
	{
		this.excluded = excluded;
		this.projects = projects.stream().filter(p -> !p.equals(excluded)).collect(Collectors.toList());
	}

	@Override
	protected List<Project> projectList()
	{
		return projects;
	}
}
