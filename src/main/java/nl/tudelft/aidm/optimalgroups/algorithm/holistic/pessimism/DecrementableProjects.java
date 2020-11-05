package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.stream.Collectors;

public class DecrementableProjects extends ListBasedProjects
{
	private final Map<Project, Integer> availableSlots;
	private final List<Project> availableProjects;

	public DecrementableProjects(Projects projects)
	{
		this(
			new ArrayList<>(projects.asCollection()),
			fullAvailability(projects)
		);
	}

	private DecrementableProjects(List<Project> availableProjects, Map<Project, Integer> availableSlots)
	{
		this.availableProjects = availableProjects;
		this.availableSlots = availableSlots;
	}

	public DecrementableProjects decremented(Project project)
	{
		var numSlotsAvailableForProject = availableSlots.get(project);

		Assert.that(numSlotsAvailableForProject != null)
			.orThrowMessage("Given project cannot be decremented, project is not present");

		Assert.that(numSlotsAvailableForProject > 0)
			.orThrowMessage("Given project cannot be decremented, project has no slots available");


		IdentityHashMap<Project, Integer> updatedSlotAvailabilities = new IdentityHashMap<>(availableSlots);
		updatedSlotAvailabilities.merge(project, -1, Integer::sum);

		var availableProjectsUpdated = updatedSlotAvailabilities.entrySet().stream()
			.filter(projAndSlotAvilability -> projAndSlotAvilability.getValue() > 0)
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());

		return new DecrementableProjects(availableProjectsUpdated, updatedSlotAvailabilities);
	}

	@Override
	protected List<Project> projectList()
	{
		return availableProjects;
	}

	private static Map<Project, Integer> fullAvailability(Projects projects)
	{
		Map<Project, Integer> availableSlots = new IdentityHashMap<>(projects.count());
		projects.forEach(project -> availableSlots.put(project, project.slots().size()));

		return availableSlots;
	}
}
