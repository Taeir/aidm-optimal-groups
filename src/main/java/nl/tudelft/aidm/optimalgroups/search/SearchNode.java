package nl.tudelft.aidm.optimalgroups.search;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp.ILPPPDeterminedMatching;

import java.util.Optional;

public abstract class SearchNode<SOLUTION>
{
	private Optional<SOLUTION> solution;

	public synchronized Optional<SOLUTION> solution() {
		//noinspection OptionalAssignedToNull
		if (solution == null) {
			solution = solve();
		}

		return solution;
	}

	abstract Optional<SOLUTION> solve();
}
