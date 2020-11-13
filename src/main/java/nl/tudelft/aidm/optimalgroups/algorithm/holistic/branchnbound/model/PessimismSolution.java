package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.search.Solution;

public record PessimismSolution(PessimismMatching matching, PessimismMetric metric) implements nl.tudelft.aidm.optimalgroups.search.Solution<PessimismMetric>
{
	public static PessimismSolution fromMatching(PessimismMatching matching)
	{
		return new PessimismSolution(matching, PessimismMetric.from(matching));
	}

	public static PessimismSolution empty(DatasetContext datasetContext)
	{
		return new PessimismSolution(new EmptyMatching(datasetContext), PessimismMetric.impossiblyBad());
	}

	public static PessimismSolution emptyWithBoundedWorstRank(DatasetContext datasetContext, int rankBound)
	{
		return new PessimismSolution(new EmptyMatching(datasetContext), PessimismMetric.boundedRank(rankBound));
	}

	@Override
	public boolean isBetterThan(Solution other)
	{
		if (other instanceof PessimismSolution otherSolution)
			return metric.betterThan(otherSolution.metric());

		throw new RuntimeException("Cannot compare solutions, type mismatch (given is not of type PessimismSolution)");
	}
}
