package nl.tudelft.aidm.optimalgroups.model.entity;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class FormedGroups implements Groups<Group.FormedGroup>
{
	private List<Group.FormedGroup> groups = new ArrayList<>();

	public Collection<Group.FormedGroup> asCollection()
	{
		return this.groups;
	}

	// make new
	public Group.FormedGroup addAsFormed(Group tentativeGroup)
	{
		var group = new Group.FormedGroup(tentativeGroup.members(), tentativeGroup.projectPreference(), groups.size());
		groups.add(group);

		return group;
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
