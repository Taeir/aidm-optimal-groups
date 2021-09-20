package nl.tudelft.aidm.optimalgroups.model.pref.rank;

import java.util.Objects;

public class PresentRankInPref implements RankInPref
{
	private final Integer rank;

	public PresentRankInPref(Integer rank)
	{
		Objects.requireNonNull(rank, "PresentRankInPref: rank must not be null (null is not a rank that is 'present')");
		this.rank = rank;
	}

	@Override
	public boolean isCompletelyIndifferent()
	{
		return false;
	}

	@Override
	public boolean unacceptable()
	{
		return false;
	}

	@Override
	public Integer asInt()
	{
		return rank;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PresentRankInPref that = (PresentRankInPref) o;
		return rank.equals(that.rank);
	}
	
	@Override
	public int hashCode()
	{
		return rank;
	}
	
	@Override
	public String toString()
	{
		return "{rank=" + rank + '}';
	}
}
