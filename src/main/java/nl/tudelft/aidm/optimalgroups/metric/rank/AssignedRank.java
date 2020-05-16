package nl.tudelft.aidm.optimalgroups.metric.rank;

import java.util.OptionalInt;

public interface AssignedRank
{
	/**
	 * An optional containing either the assigned rank, or is empty
	 * @return
	 */
	OptionalInt asInt();

	/**
	 * Indicates if the rank matters, that is if the agent is indifferent
	 * @return
	 */
	boolean isOfIndifferentAgent();
}
