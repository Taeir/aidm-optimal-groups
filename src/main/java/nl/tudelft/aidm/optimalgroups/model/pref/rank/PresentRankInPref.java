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
}
