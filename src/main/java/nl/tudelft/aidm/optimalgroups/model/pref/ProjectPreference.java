package nl.tudelft.aidm.optimalgroups.model.pref;

import javax.sql.DataSource;

public class ProjectPreference
{
	// TODO: determine representation (let algo guide this choice)

	public static class fromDb extends ProjectPreference
	{
		private final DataSource dataSource;
		private final String userId;
		private final String courseEditionId;

		public fromDb(DataSource dataSource, String userId, String courseEditionId)
		{
			this.dataSource = dataSource;
			this.userId = userId;
			this.courseEditionId = courseEditionId;
		}
	}
}
