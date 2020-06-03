package nl.tudelft.aidm.optimalgroups.model.group;

import plouchtch.assertion.Assert;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TentativeGroups extends Groups.ListBacked<Group.TentativeGroup>
{
	private List<Group.TentativeGroup> groups;

	public TentativeGroups()
	{
		this(new ArrayList<>());
	}

	private TentativeGroups(List<Group.TentativeGroup> groups)
	{
		this.groups = groups;

		assertNoAgentInMultipleGroups();
	}

	public List<Group.TentativeGroup> asList()
	{
		return Collections.unmodifiableList(this.groups);
	}

	/**
	 * Turns the given group into a TentativeGroup and makes it part of this collection. <br />
	 * This method checks if an agent is part of more than one group, but does not check group size
	 * constraints!
	 * @param tentativeGroup The group to be added
	 * @return The group as a formed group
	 */
	public Group.TentativeGroup addAsTentative(Group tentativeGroup)
	{
		var group = new Group.TentativeGroup(tentativeGroup.members(), tentativeGroup.projectPreference());
		groups.add(group);

		assertNoAgentInMultipleGroups();

		return group;
	}

	public TentativeGroups without(Group group)
	{
		List<Group.TentativeGroup> withoutGroup = groups.stream()
			.filter(inList -> group != inList)
			.collect(Collectors.toList());

		return new TentativeGroups(withoutGroup);
	}

	public int countDistinctStudents()
	{
		return (int) groups.stream()
			.flatMap(formedGroup -> formedGroup.members.asCollection().stream())
			.distinct()
			.count();
	}

	private void assertNoAgentInMultipleGroups()
	{
		int numDistinctStudents = this.countDistinctStudents();
		int numGrossStudents = groups.stream().mapToInt(formedGroup -> formedGroup.members().count()).sum();
		Assert.that(numDistinctStudents == numGrossStudents).orThrowMessage("Bugcheck: some student(s) is contained in multiple groups");
	}
}
