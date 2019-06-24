package nl.tudelft.aidm.optimalgroups.model.match;

import java.util.*;

/**
 * The result of a matchings algorithm
 */
public interface Matchings<FROM, TO>
{
	/**
	 * The matchings as a list representation
	 * @return An unmodifiable list
	 */
	List<Match<FROM, TO>> asList();
}
