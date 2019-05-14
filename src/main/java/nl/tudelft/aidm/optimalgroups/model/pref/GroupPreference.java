package nl.tudelft.aidm.optimalgroups.model.pref;

import javax.sql.DataSource;

public class GroupPreference
{
	// TODO: determine representation (let algo guide this choice)

	public static class fromDb extends GroupPreference
	{
		private DataSource dataSource;
		private String userId;
		private String courseEditionId;

		public fromDb(DataSource dataSource, String userId, String courseEditionId)
		{
			this.dataSource = dataSource;
			this.userId = userId;
			this.courseEditionId = courseEditionId;
		}
	}
}
