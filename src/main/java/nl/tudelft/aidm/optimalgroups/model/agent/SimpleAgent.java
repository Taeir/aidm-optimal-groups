package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.*;

public abstract class SimpleAgent implements Agent
{
	private final DatasetContext context;
	
	private final Integer sequenceNumber;
	
	private final ProjectPreference projectPreference;
	private final GroupPreference groupPreference;
	
	// TODO: remove, transform agent with a new datasetcontext
	private boolean usingCombinedPreference = false;
	private CombinedPreference combinedPreference = null;

	protected SimpleAgent(Agent agent)
	{
		this(agent.sequenceNumber(), agent.projectPreference(), agent.groupPreference(), agent.datasetContext());

		// todo: very iffy
		usingCombinedPreference = agent instanceof SimpleAgent ? ((SimpleAgent) agent).usingCombinedPreference : false;
		combinedPreference = usingCombinedPreference ? (CombinedPreference) agent.projectPreference() : null;
	}

	protected SimpleAgent(Integer sequenceNumber, ProjectPreference projectPreference, GroupPreference groupPreference, DatasetContext context)
	{
		this.sequenceNumber = sequenceNumber;
		this.projectPreference = projectPreference;
		this.groupPreference = groupPreference;
		this.context = context;
	}

	@Override
	public void replaceProjectPreferenceWithCombined(Agents agents)
	{
		this.combinedPreference = new CombinedPreference(this.groupPreference(), this.projectPreference, agents);
		this.usingCombinedPreference = true;
	}

	@Override
	public void useDatabaseProjectPreferences()
	{
		this.usingCombinedPreference = false;
	}

	@Override
	public int groupPreferenceLength()
	{
		return this.groupPreference().asArray().length;
	}

	@Override
	public ProjectPreference projectPreference()
	{
		return (usingCombinedPreference) ? this.combinedPreference : this.projectPreference;
	}
	
	@Override
	public GroupPreference groupPreference()
	{
		return groupPreference;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if ((obj instanceof SimpleAgent) == false) return false;

		Agent that = (Agent) obj;
		return this.datasetContext().equals(that.datasetContext()) && this.sequenceNumber().equals(that.sequenceNumber());
	}

	@Override
	public String toString()
	{
		return "agent_" + sequenceNumber();
	}
	
	/**
	 * The sequence number of this agent, a sort of a local identifier
	 * within the context of the dataset. That is, in a dataset the agents are numbered 1 to N
	*/
	@Override
	public Integer sequenceNumber()
	{
		return sequenceNumber;
	}
	
	@Override
	public DatasetContext datasetContext()
	{
		return context;
	}
	
}
