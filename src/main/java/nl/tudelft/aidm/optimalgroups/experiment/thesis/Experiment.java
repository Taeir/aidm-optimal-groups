package nl.tudelft.aidm.optimalgroups.experiment.thesis;

import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.metric.Distribution;
import nl.tudelft.aidm.optimalgroups.metric.dataset.AvgPreferenceRankOfProjects;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An experiment consists of three main parts:
 * <lu>
 *      <li>The input - the data with which the experiment is run. Which is a {@code DatasetContext}</li>
 *      <li>The experiment itself - that is, the operation to be done.
 *          In our case, these are the algorithms we want to execute on the data.
 *          Each experiment supports executing one or more algorithms on the same input data.
*       </li>
 *      <li>The results - the output of the experiment. Each algorithm generates its own result,
 *          hence the experiment's result will contain just as many results as there are algorithms
 *          in the experiment
 *      </li>
 * </lu>
 */
public class Experiment
{
	public final DatasetContext datasetContext;
	public final List<GroupProjectAlgorithm> matchingAlgorithms;
	public final AvgPreferenceRankOfProjects projectRankingDistribution;

	private ExperimentResult result;

	// unsupported for now
	private final int numIterations = 1;

	public Experiment(DatasetContext datasetContext, List<GroupProjectAlgorithm> matchingAlgorithms)
	{
		this.datasetContext = datasetContext;
		this.matchingAlgorithms = Collections.unmodifiableList(matchingAlgorithms);
		projectRankingDistribution = AvgPreferenceRankOfProjects.ofAgentsInDatasetContext(datasetContext);
	}

	public ExperimentResult result()
	{
		// Already have the result?
		if (result != null) {
			return result;
		}

		// Compute result
		var results = matchingAlgorithms.stream()
			.map(algorithm -> {
				var resultingMatching = algorithm.determineMatching(datasetContext);
				return new ExperimentAlgorithmSubresult(algorithm, resultingMatching);
			})
			.collect(Collectors.toList());

		this.result = new ExperimentResult(this, results);
		return result;
	}

	private void printDatasetInfo(PrintStream printStream)
	{
		printStream.println("Amount of projects: " + datasetContext.allProjects().count());
		printStream.println("Amount of students: " + datasetContext.allAgents().count());
	}

	public static void writeToFile(String fileName, String content) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
			writer.write(content);
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void printIterationNumber(int iteration)
	{
		System.out.printf("Iteration %d\n", iteration+1);
	}

	private void printAveragePeerSatisfaction(Distribution.AverageDistribution groupPreferenceSatisfactionDistribution)
	{
		System.out.println("Average group preference satisfaction: " + groupPreferenceSatisfactionDistribution.average());
	}

	private void printGroupAupcrAverage(float groupAUPCRAverage)
	{
		System.out.printf("AUPCR - Group aggregate pref (average over %d iterations: %f)\n", numIterations, groupAUPCRAverage);
	}

	private void printStudentAupcrAverage(float studentAUPCRAverage)
	{
		System.out.printf("AUPCR - Individual student   (average over %d iterations: %f)\n", numIterations, studentAUPCRAverage);
	}
}
