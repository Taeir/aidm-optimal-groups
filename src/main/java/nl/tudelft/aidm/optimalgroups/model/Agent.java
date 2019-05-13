package nl.tudelft.aidm.optimalgroups.model;

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
