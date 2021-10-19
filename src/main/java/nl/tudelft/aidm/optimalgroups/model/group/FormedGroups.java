package nl.tudelft.aidm.optimalgroups.model.group;

import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FormedGroups implements Groups<Group.FormedGroup>
{
	private final Object lock = new Object();
	private Integer nextGroupId = 0;

	private List<Group.FormedGroup> groups;

	public FormedGroups()
	{
		this(new ArrayList<>());
	}

	private FormedGroups(List<Group.FormedGroup> groups)
	{
		this.groups = groups;
		this.groups.forEach(this::assertGroupIsValid);

		assertNoAgentInMultipleGroups();
	}

	public Collection<Group.FormedGroup> asCollection()
	{
		return Collections.unmodifiableList(this.groups);
	}

	@Override
	public Agents asAgents()
	{
		var agents = this.groups.stream()
			.map(Group::members)
			.collect(Agents::from, Agents::with,  Agents::with);

		return agents;
	}

	/**
	 * Turns the given group into a FormedGroup and makes it part of this collection. <br />
	 * This method checks if an agent is part of more than one group, and if the given group
	 * adheres to group-size-constraints.
	 * @param tentativeGroup The group to be added
	 * @return The group as a formed group
	 */
	public Group.FormedGroup addAsFormed(Group tentativeGroup)
	{
		int groupId;
		synchronized (lock) {
			groupId = nextGroupId++;
		}

		var group = new Group.FormedGroup(tentativeGroup.members(), tentativeGroup.projectPreference(), groupId);
		groups.add(group);

		assertGroupIsValid(group);
		assertNoAgentInMultipleGroups();

		return group;
	}

	public FormedGroups without(Group group)
	{
		List<Group.FormedGroup> withoutGroup = groups.stream()
			.filter(inList -> group != inList)
			.collect(Collectors.toList());

		return new FormedGroups(withoutGroup);
	}

	public int count()
	{
		return groups.size();
	}

	public int countDistinctStudents()
	{
		return (int) groups.stream()
			.flatMap(formedGroup -> formedGroup.members.asCollection().stream())
			.distinct()
			.count();
	}

	public void forEach(Consumer<Group.FormedGroup> fn)
	{
		this.groups.forEach(fn);
	}

	private void assertNoAgentInMultipleGroups()
	{
		int numDistinctStudents = this.countDistinctStudents();
		int numGrossStudents = groups.stream().mapToInt(formedGroup -> formedGroup.members().count()).sum();
		Assert.that(numDistinctStudents == numGrossStudents).orThrowMessage("Bugcheck: some student(s) is contained in multiple groups");
	}

	private void assertGroupIsValid(Group group)
	{
		var datasetContext = group.members().asCollection().stream().map(agent -> agent.datasetContext()).findAny().orElseThrow();
		var groupSizeConstraint = datasetContext.groupSizeConstraint();

		int numMembers = group.members().count();

		Assert.that(groupSizeConstraint.minSize() <= numMembers && numMembers <= groupSizeConstraint.maxSize())
			.orThrowMessage("Cannot form group: the group does not meet group size constraints");
	}
}
