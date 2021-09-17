package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref.GroupPreferenceInDb;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref.RawProjectPreferencesInDb;
import nl.tudelft.aidm.optimalgroups.model.HasProjectPrefs;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.*;
import nl.tudelft.aidm.optimalgroups.model.pref.base.ListBasedProjectPreferences;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public abstract class Agent implements HasProjectPrefs
{
	public final DatasetContext context;
	
	/**
	 * The sequence number of this agent, a sort of a local identifier
	 * within the context of the dataset. So, in a dataset agents are numbered 1 till N
	*/
	public final Integer sequenceNumber;
	
	private final ProjectPreference projectPreference;
	public final GroupPreference groupPreference;
	
	// TODO: remove, transform agent with a new datasetcontext
	private boolean usingCombinedPreference = false;
	private CombinedPreference combinedPreference = null;

	protected Agent(Agent agent)
	{
		this(agent.sequenceNumber, agent.projectPreference, agent.groupPreference, agent.context);

		usingCombinedPreference = agent.usingCombinedPreference;
		combinedPreference = agent.combinedPreference;
	}

	protected Agent(Integer sequenceNumber, ProjectPreference projectPreference, GroupPreference groupPreference, DatasetContext context)
	{
		this.sequenceNumber = sequenceNumber;
		this.projectPreference = projectPreference;
		this.groupPreference = groupPreference;
		this.context = context;
	}

	public void replaceProjectPreferenceWithCombined(Agents agents)
	{
		this.combinedPreference = new CombinedPreference(this.groupPreference, this.projectPreference, agents);
		this.usingCombinedPreference = true;
	}

	public void useDatabaseProjectPreferences()
	{
		this.usingCombinedPreference = false;
	}

	public int groupPreferenceLength()
	{
		return this.groupPreference.asArray().length;
	}

	@Override
	public ProjectPreference projectPreference()
	{
		return (usingCombinedPreference) ? this.combinedPreference : this.projectPreference;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if ((obj instanceof Agent) == false) return false;

		Agent that = (Agent) obj;
		return this.context.equals(that.context) && this.sequenceNumber.equals(that.sequenceNumber);
	}

	@Override
	public String toString()
	{
		return "agent_" + sequenceNumber;
	}

	/**
	 * Represents an Agent whose data is retrieved from a data source
	 */
	public static class AgentInBepSysSchemaDb extends Agent
	{
		private final DataSource dataSource;
		
		/**
		 * This agent comes from a BepSys/PF database and has a user_id. For such datasets,
		 * we need to communicate the matching back to the other application and for that
		 * we need the original identifier.
		 */
		public final Integer bepSysUserId;

		public AgentInBepSysSchemaDb(DataSource dataSource, Integer sequenceNumber, Integer bepSysUserId, CourseEdition courseEdition)
		{
			super(
				sequenceNumber,
				new RawProjectPreferencesInDb(dataSource, bepSysUserId, courseEdition),
				new GroupPreferenceInDb(dataSource, bepSysUserId, courseEdition),
				courseEdition
			);
			
			this.dataSource = dataSource;
			this.bepSysUserId = bepSysUserId;
		}
		
		public AgentInBepSysSchemaDb(AgentInBepSysSchemaDb agentToCopy, CourseEdition newCourseEdition)
		{
			this(agentToCopy.dataSource, agentToCopy.sequenceNumber, agentToCopy.bepSysUserId, newCourseEdition);
		}
		
		@Override
		public String toString()
		{
			return "student_" + bepSysUserId;
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

	public static class AgentInDatacontext extends Agent
	{
		public AgentInDatacontext(Integer sequenceNumber, ProjectPreference projectPreference, GroupPreference groupPreference, DatasetContext context)
		{
			super(sequenceNumber, projectPreference, groupPreference, context);
		}
	}
}
