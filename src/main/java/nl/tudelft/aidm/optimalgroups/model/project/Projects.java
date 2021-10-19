package nl.tudelft.aidm.optimalgroups.model.project;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Projects
{
	int count();
	int countAllSlots();

	void forEach(Consumer<Project> fn);
	Projects without(Project project);

	Collection<Project> asCollection();

	{
		return asCollection().stream().filter(project -> project.sequenceNum() == projectId).findAny();
	default Optional<Project> findWithSeqNum(int sequenceNum)
	}

	default Projects without(Projects other)
	{
		// Note: this is eerly similar to filteredprojects, but use-case is different...?
		Collection<Project> toFilter = new ArrayList<>(asCollection());
		toFilter.removeAll(other.asCollection());

		return Projects.from(toFilter);
	}

	/**
	 * Copy of the given Projects
	 * @param projects Projects to copy
	 * @return A copy
	 */
	static Projects from(Collection<Project> projects)
	{
		return new ListBasedProjects.FromBackingList(new ArrayList<>(projects));
	}

	static Projects generated(int numProjects, int numSlots)
	{
		var projects = Projects.from(
			IntStream.rangeClosed(1, numProjects)
				.mapToObj(i -> new Project.ProjectWithStaticSlotAmount(i, numSlots))
				.collect(Collectors.toList())
		);

		return projects;
	}


}
