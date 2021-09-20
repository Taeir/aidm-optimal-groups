package nl.tudelft.aidm.optimalgroups.model.pref.rank;

import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class UnacceptableAlternativeRank implements RankInPref
{
	private final Object owner;
	private final Project project;

	public UnacceptableAlternativeRank(Object owner, Project project)
	{
		this.owner = owner;
		this.project = project;
	}

	@Override
	public boolean isCompletelyIndifferent()
	{
		return false;
	}

	@Override
	public boolean unacceptable()
	{
		return true;
	}

	@Override
	public Integer asInt()
	{
		throw new RuntimeException(
			String.format("Project (%s) is unacceptable to agent (%s), therefore no rank is present. Please use the unacceptable() method to check first",
				project, owner)
		);
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof UnacceptableAlternativeRank;
	}
	
	@Override
	public int hashCode()
	{
		return 0;
	}
}
