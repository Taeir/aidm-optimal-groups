package nl.tudelft.aidm.optimalgroups.model.pref.rank;

public class UnacceptableAlternativeRank implements RankInPref
{
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
			"Project is unacceptable to agent, therefore no rank is present. Handle by making use of the .unacceptable() method first"
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
