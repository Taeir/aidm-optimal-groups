package nl.tudelft.aidm.optimalgroups.model.entity;

import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

public class Agent
{
	public final ProjectPreference projectPreference;
	public final GroupPreference groupPreference;

	private Agent(ProjectPreference projectPreference, GroupPreference groupPreference)
	{
		this.projectPreference = projectPreference;
		this.groupPreference = groupPreference;
	}

	public Agent withPreferences(ProjectPreference projectPreference, GroupPreference groupPreference)
	{
		return new Agent(projectPreference, groupPreference);
	}
}
