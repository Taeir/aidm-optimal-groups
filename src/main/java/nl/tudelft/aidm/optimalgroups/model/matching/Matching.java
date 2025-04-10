package nl.tudelft.aidm.optimalgroups.model.matching;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;

import java.util.List;

/**
 * The result of a matchings algorithm
 */
public interface Matching<FROM, TO>
{
	/**
	 * The matchings as a list representation
	 * @return An unmodifiable list
	 */
	List<Match<FROM, TO>> asList();

	/**
	 * The dataset the matching was created from
	 * @return The source dataset
	 */
	DatasetContext datasetContext();
}
