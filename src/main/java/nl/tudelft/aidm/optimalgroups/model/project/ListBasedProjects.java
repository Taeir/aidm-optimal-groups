package nl.tudelft.aidm.optimalgroups.model.project;

import java.util.*;
import java.util.function.Consumer;

/**
 * An ArrayList based impl
 */
public abstract class ListBasedProjects implements Projects
{
	private Map<Integer, Project> projectsById = null;

	abstract protected List<Project> projectList();

	public int count()
	{
		return projectList().size();
	}

	@Override
	public void forEach(Consumer<Project> fn)
	{
		projectList().forEach(fn);
	}

	@Override
	public Projects without(Project toExclude)
	{
		return new FilteredProjects(this.projectList(), toExclude);
	}

	@Override
	public List<Project.ProjectSlot> slotsForProject(int projectId) {
		String projectName = "proj_" + String.valueOf(projectId);
		Project project = this.projectList().stream()
			.filter(p -> p.name().equals(projectName))
			.findAny().get();

		return project.slots();
	}

	private int numTotalSlots = -1;

	@Override
	public int countAllSlots()
	{
		// lazy eval
		if (numTotalSlots < 0)
		{
			numTotalSlots = projectList().stream()
				.map(project -> project.slots().size())
				.mapToInt(Integer::intValue)
				.sum();
		}

		return numTotalSlots;
	}

	@Override
	public Collection<Project> asCollection()
	{
		return Collections.unmodifiableCollection(projectList());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects))
		{
			return false;
		}
		nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects that = (nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects) o;
		return numTotalSlots == that.numTotalSlots && projectList().equals(that.projectList());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(projectList());
	}

	@Override
	public Optional<Project> findWithId(int projectId)
	{
		if (projectsById == null) {
			// Lazy eval: only fill the map if method is used
			projectsById = new HashMap<>();
			projectList().forEach(project ->
					projectsById.put(project.id(), project)
			);
		}

		Project value = projectsById.get(projectId);
		return Optional.ofNullable(value);
	}

	/**
	 * A simple implementation of ListBasedProjects
	 */
	public static class FromBackingList extends ListBasedProjects
	{
		private final List<Project> projects;

		public FromBackingList(List<Project> projects)
		{
			this.projects = projects;
		}

		@Override
		protected List<Project> projectList()
		{
			return projects;
		}
	}
}
