package nl.tudelft.aidm.optimalgroups.model.pref.rank;

import java.util.OptionalInt;

public interface RankInPref
{
	/**
	 * Indicates the agent does not have preferences - indifferent over all alternatives
	 * @return true if agent doesn't care - any rank is ok
	 */
	boolean isCompletelyIndifferent();

	/**
	 * The alternative is not present in agent's preference - the agent deems the alternative as not acceptable
	 * @return true if alternative is unacceptable
	 */
	boolean unacceptable();

	/**
	 * Rank of the alternative in agent's preference - this rank might be tied between other alternatives
	 * @return The rank if present. It is not present if agent is neither completely indifferent or is unaccepable to the agent
	 * @throws RuntimeException If the rank is not present
	 */
	Integer asInt();

	/**
	 * Indicates weather a proper rank is present (agent is not completely indifferent and deemed the project acceptable)
	 * @return True is a rank is present (shorthand for: {@code !(isCompletelyIndifferent() || unacceptable())}
	 */
	default boolean isPresent()
	{
		return !(isCompletelyIndifferent() || unacceptable());
	}

}
