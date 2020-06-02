package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref.GroupPreferenceInDb;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref.ProjectPreferencesInDb;
import nl.tudelft.aidm.optimalgroups.model.HasProjectPrefs;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.*;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public abstract class Agent implements HasProjectPrefs
{
	public final Integer id;
	private final ProjectPreference projectPreference;
	public final GroupPreference groupPreference;

	public final DatasetContext context;

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

	/**
	 * Is the group proposed by the agent is mutual? That is, do the agents
	 * that form part of the proposal have identical proposal? A.k.a. "a clique" of friends
	 * @return True if "friends" have 'identical' peer preferences
	 */
	public boolean groupProposalIsMutual()
	{
		if(this.groupPreference.count() == 0) {
			// No preference, therefore also no clique
			return false;
		}

		// Function that maps the Agent's "group preferences" to a Set of agents (including himself)
		// intuitively, this is the agent's proposal for a (partial) group formation.
		Function<Agent, Set<Agent>> agentPreferencesToProposedGroup = (Agent x) -> {
			var groupProposal = new HashSet<Agent>();
			//Add agent himself to set to make comparing preferences easy
			groupProposal.add(x);
			groupProposal.addAll(x.groupPreference.asList());

			return groupProposal;
		};

		// The proposal of the given agent
		var proposedGroupOfAgent = agentPreferencesToProposedGroup.apply(this);

		// If all the agents that are in the proposal of 'this agent' have _exactly_ the
		// same proposals, then
		var agentProposalIsCompletelyMutual = this.groupPreference.asList().stream()
			.map(agentPreferencesToProposedGroup)
			.allMatch(proposedGroupOfAgent::equals);

			return agentProposalIsCompletelyMutual;
	}

	@Override
	public ProjectPreference projectPreference() {
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

	public static class AgentInDatacontext extends Agent
	{
		public AgentInDatacontext(Integer id, ProjectPreference projectPreference, GroupPreference groupPreference, DatasetContext context)
		{
			super(id, projectPreference, groupPreference, context);
		}
	}
}
