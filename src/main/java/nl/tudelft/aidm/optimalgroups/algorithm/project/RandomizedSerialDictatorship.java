package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.FormedGroupToProjectSlotMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.FormedGroupToProjectSlotMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RandomizedSerialDictatorship implements GroupToProjectMatching<Group.FormedGroup>
{
	private final DatasetContext datasetContext;
	private final FormedGroups groups;
	private final Projects projects;

	public RandomizedSerialDictatorship(DatasetContext datasetContext, FormedGroups groups, Projects projects)
	{
		this.datasetContext = datasetContext;
		this.groups = groups;
		this.projects = projects;
	}

	@Override
	public List<Match<Group.FormedGroup, Project>> asList()
	{
		return result().toProjectMatchings().asList();
	}

	public FormedGroupToProjectSlotMatching result()
	{
		if (this.projects.countAllSlots() < this.groups.count())
			throw new RuntimeException("Too little project slots to assign all groups");

		FormedGroupToProjectSlotMatching result = new FormedGroupToProjectSlotMatching(datasetContext());

		// Map from projectIds to amount of used slots
		Map<Project, Integer> usedSlots = new IdentityHashMap<>();

		List<Group.FormedGroup> shuffledGroups = new ArrayList<>(this.groups.asCollection());
		Collections.shuffle(shuffledGroups);
		
		// Iterate over the groups in a random order
		for (Group.FormedGroup group : shuffledGroups) {
			
			// Iterate the preference in order, assign as soon as possible
			group.projectPreference().forEach((project, rank, __) ->
			{
				int currentlyUsedSlots = usedSlots.getOrDefault(project, 0);

				// If there is still a spot available for this project
				if (currentlyUsedSlots < project.slots().size()) {

					// Retrieve the slot to use (if the currentlyUsedSlots is 0, get index 0, etc)
					var unusedSlot = project.slots().get(currentlyUsedSlots);
					FormedGroupToProjectSlotMatch newMatch = new FormedGroupToProjectSlotMatch(group, unusedSlot);
					result.add(newMatch);
					
					usedSlots.put(project, currentlyUsedSlots + 1);
					
					__._break(); // stop iteration
					return;
				}
				
			});
		}

		return result;
	}

	@Override
	public DatasetContext datasetContext()
	{
		return datasetContext;
	}
}
