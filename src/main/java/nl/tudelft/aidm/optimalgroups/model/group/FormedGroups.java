package nl.tudelft.aidm.optimalgroups.model.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

	public FormedGroups(List<Group.FormedGroup> groups)
	{
		this.groups = groups;
	}

	public Collection<Group.FormedGroup> asCollection()
	{
		return Collections.unmodifiableList(this.groups);
	}

	// make new
	public Group.FormedGroup addAsFormed(Group tentativeGroup)
	{
		int groupId;
		synchronized (lock) {
			groupId = nextGroupId++;
		}

		var group = new Group.FormedGroup(tentativeGroup.members(), tentativeGroup.projectPreference(), groupId);
		groups.add(group);

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

	public int countTotalStudents()
	{
		return groups.stream().mapToInt(formedGroup -> formedGroup.members().count()).sum();
	}

	public void forEach(Consumer<Group.FormedGroup> fn)
	{
		this.groups.forEach(fn);
	}
}
