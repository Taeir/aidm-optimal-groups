package nl.tudelft.aidm.optimalgroups.algorithm.project;

import java.util.List;

/**
 * The result of a matching algorithm
 */
public interface Matching
{
	List<Match> asList();

	/**
	 * The match between a group/agent and project
	 */
	public interface Match
	{
		String agent();
		String project();
	}
}
