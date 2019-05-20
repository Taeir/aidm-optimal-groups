package nl.tudelft.aidm.optimalgroups.model.entity;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.support.ImplementMe;

public class Group
{
	private final int id;
	private Agents members;
	private ProjectPreference projectPreference;

	Group(int id, Agents members, ProjectPreference projectPreference)
	{
		this.id = id;
		this.members = members;
		this.projectPreference = projectPreference;
	}

	public Agents members()
	{
		return members;
	}

	public ProjectPreference projectPreference()
	{
		return projectPreference;
	}
}
