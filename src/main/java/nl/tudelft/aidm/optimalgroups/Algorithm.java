package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.model.matching.Matching;

public interface Algorithm
{
	String name();

	interface Result<A extends Algorithm, R extends Matching> {
		Algorithm algo();
		R producedMatching();
	}
}
