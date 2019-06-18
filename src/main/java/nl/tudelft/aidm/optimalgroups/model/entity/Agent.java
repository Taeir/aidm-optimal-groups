package nl.tudelft.aidm.optimalgroups.model.entity;

import nl.tudelft.aidm.optimalgroups.model.pref.CombinedPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import javax.sql.DataSource;

public abstract class Agent
{
	public final String name;
	public final ProjectPreference projectPreference;
	public final GroupPreference groupPreference;

	private CombinedPreference combinedPreference;
	private boolean usingCombinedPreference = false;

	protected Agent(String name, ProjectPreference projectPreference, GroupPreference groupPreference)
	{
		this.name = name;
		this.projectPreference = projectPreference;
		this.groupPreference = groupPreference;
	}

	public void replaceProjectPreferenceWithCombined(Agents agents) {
		this.combinedPreference = new CombinedPreference(this.groupPreference, this.projectPreference, agents);
		this.usingCombinedPreference = true;
	}

	public void useDatabaseProjectPreferences() {
		this.usingCombinedPreference = false;
	}

	public ProjectPreference getProjectPreference() {
		return (usingCombinedPreference) ? this.combinedPreference : this.projectPreference;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if ((obj instanceof Agent) == false) return false;

		Agent that = (Agent) obj;
		return this.name.equals(that.name);
	}

	@Override
	public String toString()
	{
		return "Agent (" + name + ")";
	}

	public int groupPreferenceLength() { return this.groupPreference.asArray().length; }

	/**
	 * Represents an Agent whose data is retrieved from a data source
	 */
	public static class fromDb extends Agent
	{
		private String userId;
		private String courseEditionId;

		public fromDb(DataSource dataSource, String userId, String courseEditionId)
		{
			super(
				userId,
				new ProjectPreference.fromDb(dataSource, userId, courseEditionId),
				new GroupPreference.fromDb(dataSource, userId, courseEditionId)
			);

			this.userId = userId;
			this.courseEditionId = courseEditionId;
		}
	}
}
