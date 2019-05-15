package nl.tudelft.aidm.optimalgroups.model.entity;

import java.util.Collection;
import java.util.List;

public class Groups
{
	private List<Group> groups;

	public Collection<Group> asCollection()
	{
		return this.groups;
	}

	public int count()
	{
		return groups.size();
	}

	public Group getByIndex(int idx)
	{
		return groups.get(idx);
	}
}
