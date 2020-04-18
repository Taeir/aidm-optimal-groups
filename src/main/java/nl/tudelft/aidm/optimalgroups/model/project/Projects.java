package nl.tudelft.aidm.optimalgroups.model.project;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Consumer;

public interface Projects
{
	int count();
	int countAllSlots();

	List<Project.ProjectSlot> slotsForProject(int projectId);

	void forEach(Consumer<Project> fn);
	Projects without(Project project);

	Collection<Project> asCollection();

	default Optional<Project> findWithId(int projectId)
	{
		return asCollection().stream().filter(project -> project.id() == projectId).findAny();
	}

	/**
	 * Projects contained in the given (bepsys) datasource
	 * @param dataSource A datasource that has same schema as bepsys
	 * @param courseEditionInt Course edition to look in
	 * @return The projects that are offered in the given course edition
	 */
	static ProjectsInDb fromDb(DataSource dataSource, int courseEditionInt)
	{
		return new ProjectsInDb(dataSource, courseEditionInt);
	}

	/**
	 * Copy of the given Projects
	 * @param projects Projects to copy
	 * @return A copy
	 */
	static Projects from(List<Project> projects)
	{
		return new ListBasedProjects.ListBasedProjectsImpl(new ArrayList<>(projects));
	}


}
