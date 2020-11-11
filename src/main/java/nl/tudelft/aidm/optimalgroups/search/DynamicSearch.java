package nl.tudelft.aidm.optimalgroups.search;

import java.util.Optional;
import java.util.function.Predicate;

/**
 *
 * @param <SOLUTION> The final solution/result - that which is returned
 * @param <CANDIDATE> The internal solution - might need postprocessing
 */
public class DynamicSearch<CANDIDATE, SOLUTION extends Solution>
{
	protected final BestSolutionSoFar bestSolutionSoFar;

	public DynamicSearch(SOLUTION emptySolution)
	{
		this.bestSolutionSoFar = new BestSolutionSoFar(emptySolution);
	}

	public class BestSolutionSoFar
	{
		private SOLUTION bestKnown;
		private boolean hasNonEmptySolution;

		public BestSolutionSoFar(SOLUTION emptySolution)
		{
			this.bestKnown = emptySolution;
			this.hasNonEmptySolution = false;
		}

		public synchronized void potentiallyUpdateBestSolution(SOLUTION another)
		{
			if (another.isBetterThan(bestKnown)) {
				System.out.printf("New best solution found: %s (was: %s)\n", another.metric(), bestKnown.metric());
				this.bestKnown = another;
				this.hasNonEmptySolution = true;
			}
		}

		/**
		 * Executes the given predicate on the current best solution
		 * Note that the operation is not synchronized as it is meant for quick
		 * tests. The solution should be compared and updated through a different method
		 * @param predicate The predicate to run with the best-so-far solution
		 * @return Result of predicate
		 */
		public boolean test(Predicate<SOLUTION> predicate)
		{
			return predicate.test(bestKnown);
		}

		public SOLUTION currentBest()
		{
			return bestKnown;
		}

		public boolean hasNonEmptySolution()
		{
			return hasNonEmptySolution;
		}
	}

	public abstract class SearchNode
	{
//		private DynamicSearch<SOLUTION> dynamicSearchMeta;
		private Optional<SOLUTION> solution;

		protected SearchNode()
		{
		}

		public synchronized Optional<SOLUTION> solution() {
			//noinspection OptionalAssignedToNull
			if (solution == null) {
				solution = solve();
			}

			return solution;
		}

		abstract public Optional<SOLUTION> solve();

		abstract protected boolean candidateSolutionTest(CANDIDATE candidateSolution);
	}
}
