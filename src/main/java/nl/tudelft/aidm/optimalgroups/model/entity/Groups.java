package nl.tudelft.aidm.optimalgroups.model.entity;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class Groups
{
	private List<Group> groups;

	public Collection<Group> asCollection()
	{
		return this.groups;
	}

	// make new
	public Group makeGroup(Agents members, ProjectPreference preference)
	{
		var group = new Group(groups.size(), members, preference);
		groups.add(group);

		return group;
	}

	public int count()
	{
		return groups.size();
	}

	public Group getByIndex(int idx)
	{
		return groups.get(idx);
	}

	public void forEach(Consumer<Group> fn)
	{
		this.groups.forEach(fn);
	}
}
