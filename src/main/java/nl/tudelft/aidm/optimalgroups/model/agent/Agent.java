package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref.GroupPreferenceInDb;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref.RawProjectPreferencesInDb;
import nl.tudelft.aidm.optimalgroups.model.HasProjectPrefs;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import javax.sql.DataSource;

public interface Agent extends HasProjectPrefs
{
	void replaceProjectPreferenceWithCombined(Agents agents);
	
	void useDatabaseProjectPreferences();
	
	int groupPreferenceLength();
	
	@Override
	ProjectPreference projectPreference();
	
	GroupPreference groupPreference();
	
	Integer sequenceNumber();
	
	DatasetContext datasetContext();
	
	/**
	 * Represents an Agent whose data is retrieved from a data source
	 */
	public static class AgentInBepSysSchemaDb extends SimpleAgent
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
			this(agentToCopy.dataSource, agentToCopy.sequenceNumber(), agentToCopy.bepSysUserId, newCourseEdition);
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
	
	public static class AgentInDatacontext extends SimpleAgent
	{
		public AgentInDatacontext(Integer sequenceNumber, ProjectPreference projectPreference, GroupPreference groupPreference, DatasetContext context)
		{
			super(sequenceNumber, projectPreference, groupPreference, context);
		}
	}
}
