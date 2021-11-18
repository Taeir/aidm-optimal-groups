package nl.tudelft.aidm.optimalgroups.model.pref.rank;

public class RankOfCompletelyIndifferentAgent implements RankInPref
{
	public RankOfCompletelyIndifferentAgent()
	{
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
		throw new RuntimeException("The agent is indifferent, it does not rank the project as anything (handle this case properly");
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof RankOfCompletelyIndifferentAgent;
	}
	
	@Override
	public int hashCode()
	{
		return 1;
	}
}
