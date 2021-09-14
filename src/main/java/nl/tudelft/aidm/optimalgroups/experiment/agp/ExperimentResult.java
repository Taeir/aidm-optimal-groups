package nl.tudelft.aidm.optimalgroups.experiment.agp;

import nl.tudelft.aidm.optimalgroups.metric.PopularityMatrix;

import java.util.Collections;
import java.util.List;

public class ExperimentResult
{
	private final Experiment experiment;

	public final List<ExperimentAlgorithmSubresult> results;
	public final PopularityMatrix<?,?,?> popularityMatrix;

	public ExperimentResult(Experiment experiment, List<ExperimentAlgorithmSubresult> results)
	{
		this.experiment = experiment;
		this.results = Collections.unmodifiableList(results);

		this.popularityMatrix = new PopularityMatrix.TopicGroup(results);
	}
}
