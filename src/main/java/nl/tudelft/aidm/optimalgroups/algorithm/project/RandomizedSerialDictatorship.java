package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.entity.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;
import nl.tudelft.aidm.optimalgroups.model.entity.Projects;

import java.util.*;

public class RandomizedSerialDictatorship implements GroupProjectMatching<Group.FormedGroup>
{
	private final FormedGroups groups;
	private final Projects projects;

	public RandomizedSerialDictatorship(FormedGroups groups, Projects projects)
	{
		this.groups = groups;
		this.projects = projects;
	}

	@Override
	public List<Match<Group.FormedGroup, Project>> asList()
	{
		return result().toProjectMatchings().asList();
	}

	public FormedGroupToProjectSlotMatchings result()
	{
		if (this.projects.countAllSlots() < this.groups.count())
			throw new RuntimeException("Too little project slots to assign all groups");

		FormedGroupToProjectSlotMatchings result = new FormedGroupToProjectSlotMatchings();

		// Map from projectIds to amount of used slots
		Map<Integer, Integer> usedSlots = new HashMap<>();

		List<Group.FormedGroup> shuffledGroups = new ArrayList<>(this.groups.asCollection());
		Collections.shuffle(shuffledGroups);

		// Iterate over the groups is a random order
		for (Group.FormedGroup group : shuffledGroups) {

			// Iterate the preference in order, assign as soon as possible
			// use standard for loop here to be able to break, idk how to do it in a foreach with a consumer function
			int[] groupPreference = group.projectPreference().asArray();
			for (int i = 0; i < groupPreference.length; i++) {
				int projectId = groupPreference[i];
				int currentlyUsedSlots = (usedSlots.containsKey(projectId)) ? usedSlots.get(projectId) : 0;

				// If there is still a spot available for this project
				if (currentlyUsedSlots < this.projects.slotsForProject(projectId).size()) {
					usedSlots.put(projectId, currentlyUsedSlots + 1);

					// Retrieve the slot to use (if the currentlyUsedSlots is 0, get index 0, etc)
					Project.ProjectSlot unusedSlot = this.projects.slotsForProject(projectId).get(currentlyUsedSlots);
					Matching.FormedGroupToProjectSlotMatch newMatch = new Matching.FormedGroupToProjectSlotMatch(group, unusedSlot);
					result.add(newMatch);
					break;
				}
			}
		}

		return result;
	}
}
