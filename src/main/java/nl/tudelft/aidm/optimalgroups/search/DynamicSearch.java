package nl.tudelft.aidm.optimalgroups.search;

import java.util.Optional;
import java.util.function.Function;
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
		private SOLUTION bestSolutionSeen;

		public BestSolutionSoFar(SOLUTION emptySolution)
		{
			this.bestSolutionSeen = emptySolution;
		}

		public synchronized void potentiallyUpdateBestSolution(Function<SOLUTION, Optional<SOLUTION>> bestSoFarSection)
		{
			bestSoFarSection.apply(this.bestSolutionSeen)
				.ifPresent(newBest -> {
					System.out.printf("New best solution found: %s (was: %s)\n", newBest.metric(), bestSolutionSeen.metric());
					this.bestSolutionSeen = newBest;
				});
		}

		public synchronized boolean test(Predicate<SOLUTION> predicate)
		{
			return predicate.test(bestSolutionSeen);
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
