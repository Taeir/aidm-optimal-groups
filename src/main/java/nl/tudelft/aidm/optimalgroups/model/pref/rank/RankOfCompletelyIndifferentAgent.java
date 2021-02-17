package nl.tudelft.aidm.optimalgroups.model.pref.rank;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.OptionalInt;

public class RankOfCompletelyIndifferentAgent implements RankInPref
{
	private Object owner;
	private Project project;

	public RankOfCompletelyIndifferentAgent(Object owner, Project project)
	{
		this.owner = owner;
		this.project = project;
	}

	@Override
	public boolean isCompletelyIndifferent()
	{
		return true;
	}

	@Override
	public boolean unacceptable()
	{
		return false;
	}

	@Override
	public Integer asInt()
	{
		throw new RuntimeException(String.format("The agent (%s) is indifferent, and has no rank for the given project (%s)", owner, project));
	}
}
