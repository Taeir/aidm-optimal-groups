package nl.tudelft.aidm.optimalgroups.experiment;

import nl.tudelft.aidm.optimalgroups.algorithm.TopicGroupAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.PopularityMatrix;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PopularitMatrix constructed from the Experiment results
 * {@inheritDoc}
 */
public class PopularityMatrixFromExperimentResults extends PopularityMatrix
{
	public PopularityMatrixFromExperimentResults(List<Experiment.ExperimentAlgorithmResult> results)
	{
		super(results.stream().map(a -> new TopicGroupAlgorithm.Result(a.algo, a.result)).collect(Collectors.toList()));
	}
}
