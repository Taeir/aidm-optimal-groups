package nl.tudelft.aidm.optimalgroups.model.entity;

import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import javax.sql.DataSource;

public abstract class Agent
{
	public final String name;
	public final ProjectPreference projectPreference;
	public final GroupPreference groupPreference;

	private Agent(String name, ProjectPreference projectPreference, GroupPreference groupPreference)
	{
		this.name = name;
		this.projectPreference = projectPreference;
		this.groupPreference = groupPreference;
	}

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
