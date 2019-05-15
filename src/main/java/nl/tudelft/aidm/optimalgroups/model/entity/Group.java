package nl.tudelft.aidm.optimalgroups.model.entity;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.support.ImplementMe;

public class Group
{
	private ProjectPreference projectPreference;

	public Agents members()
	{
		throw new ImplementMe();
	}

	public ProjectPreference projectPreference()
	{
		return projectPreference;
	}
}
