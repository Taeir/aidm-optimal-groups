package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref.GroupPreferenceInDb;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref.ProjectPreferencesInDb;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.*;

import javax.sql.DataSource;

public abstract class Agent
{
	public final Integer id;
	public final ProjectPreference projectPreference;
	public final GroupPreference groupPreference;

	protected final DatasetContext context;

	// TODO: extract to own type?
	private boolean usingCombinedPreference = false;
	private CombinedPreference combinedPreference = null;

	protected Agent(Agent agent)
	{
		this(agent.id, agent.projectPreference, agent.groupPreference, agent.context);

		usingCombinedPreference = agent.usingCombinedPreference;
		combinedPreference = agent.combinedPreference;
	}

	protected Agent(Integer id, ProjectPreference projectPreference, GroupPreference groupPreference, DatasetContext context)
	{
		this.id = id;
		this.projectPreference = projectPreference;
		this.groupPreference = groupPreference;
		this.context = context;
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
		return this.context.equals(that.context) && this.id.equals(that.id);
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

		public AgentInBepSysSchemaDb(DataSource dataSource, Integer userId, CourseEdition courseEdition)
		{
			super(
				userId,
				new ProjectPreferencesInDb(dataSource, userId, courseEdition),
				new GroupPreferenceInDb(dataSource, userId, courseEdition),
				courseEdition
			);

			this.userId = userId;
		}

//		private static DataSource datasourceOfCache;
//		private static final HashMap<String, AgentInBepSysSchemaDb> cache = new HashMap<>();
//		public static Agent from(DataSource dataSource, Integer userId, Integer courseEditionId)
//		{
//			if (datasourceOfCache == null) {
//				datasourceOfCache = dataSource;
//			}
//
//			Assert.that(datasourceOfCache == dataSource)
//				.orThrow(RuntimeException.class, "Agents are cached for a different datasource! Please fix the cache impl to support this use case.");
//
//			return cache.computeIfAbsent(String.format("%s_%s", courseEditionId, userId),
//				(__) -> new AgentInBepSysSchemaDb(dataSource, userId, courseEditionId)
//			);
//		}
	}
}
