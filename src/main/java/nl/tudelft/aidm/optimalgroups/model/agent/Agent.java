package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.model.pref.CombinedPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import plouchtch.assertion.Assert;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.util.HashMap;

public abstract class Agent
{
	public final Integer id;
	public final ProjectPreference projectPreference;
	public final GroupPreference groupPreference;

	private CombinedPreference combinedPreference;
	private boolean usingCombinedPreference = false;

	protected Agent(Integer id, ProjectPreference projectPreference, GroupPreference groupPreference)
	{
		this.id = id;
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
		return this.id.equals(that.id);
	}

	@Override
	public String toString()
	{
		return "Agent (" + id + ")";
	}

	public int groupPreferenceLength() {
		return this.groupPreference.asArray().length;
	}

	/**
	 * Represents an Agent whose data is retrieved from a data source
	 */
	public static class AgentInBepSysSchemaDb extends Agent
	{
		private Integer userId;
		private String courseEditionId;

		private AgentInBepSysSchemaDb(DataSource dataSource, Integer userId, String courseEditionId)
		{
			super(
				userId,
				new ProjectPreference.fromDb(dataSource, userId, courseEditionId),
				new GroupPreference.fromDb(dataSource, userId, courseEditionId)
			);

			this.userId = userId;
			this.courseEditionId = courseEditionId;
		}

		private static DataSource datasourceOfCache;
		private static final HashMap<String, AgentInBepSysSchemaDb> cache = new HashMap<>();
		public static Agent from(DataSource dataSource, Integer userId, String courseEditionId)
		{
			if (datasourceOfCache == null) {
				datasourceOfCache = dataSource;
			}

			Assert.that(datasourceOfCache == dataSource)
				.orThrow(RuntimeException.class, "Agents are cached for a different datasource! Please fix the cache impl to support this use case.");

			return cache.computeIfAbsent(String.format("%s_%s", courseEditionId, userId),
				(__) -> new AgentInBepSysSchemaDb(dataSource, userId, courseEditionId)
			);
		}
	}
}
