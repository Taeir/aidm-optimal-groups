package nl.tudelft.aidm.optimalgroups.model.project;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Projects
{
	Collection<Project> asCollection();
	
	default int count()
	{
		return this.asCollection().size();
	}
	
	default int countAllSlots()
	{
		return (int) this.asCollection().stream()
				.mapToLong(project -> project.slots().size())
				.sum();
	}

	default void forEach(Consumer<Project> fn)
	{
		this.asCollection().forEach(fn);
	}

	default Optional<Project> findWithSeqNum(int sequenceNum)
	{
		return asCollection().stream().filter(project -> project.sequenceNum() == sequenceNum).findAny();
	}
	
	default Projects without(Project project)
	{
		return this.without(Projects.from(List.of(project)));
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
	
	static final ProjectsCollector collector = new ProjectsCollector();
	
	class ProjectsCollector implements Collector<Project, List<Project>, Projects>
	{
		@Override
		public Supplier<List<Project>> supplier()
		{
			return LinkedList::new;
		}
		
		@Override
		public BiConsumer<List<Project>, Project> accumulator()
		{
			return List::add;
		}
		
		@Override
		public BinaryOperator<List<Project>> combiner()
		{
			return (projects, projects2) -> {
				projects.addAll(projects2);
				return projects;
			};
		}
		
		@Override
		public Function<List<Project>, Projects> finisher()
		{
			return Projects::from;
		}
		
		@Override
		public Set<Characteristics> characteristics()
		{
			return Set.of();
		}
	}


}
