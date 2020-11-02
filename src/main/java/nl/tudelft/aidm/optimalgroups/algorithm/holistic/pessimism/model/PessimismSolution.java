package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism.model;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;

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
}
