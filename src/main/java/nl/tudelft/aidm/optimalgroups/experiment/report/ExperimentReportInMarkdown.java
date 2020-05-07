package nl.tudelft.aidm.optimalgroups.experiment.report;

import net.steppschuh.markdowngenerator.Markdown;
import nl.tudelft.aidm.optimalgroups.experiment.Experiment;
import nl.tudelft.aidm.optimalgroups.experiment.ExperimentAlgorithmSubresult;
import nl.tudelft.aidm.optimalgroups.metric.PopularityMatrix;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import plouchtch.assertion.Assert;

import java.util.List;

public class ExperimentReportInMarkdown
{
	private final List<Experiment> experiments;

	public ExperimentReportInMarkdown(Experiment... experiments)
	{
		this(List.of(experiments));
	}

	public ExperimentReportInMarkdown(List<Experiment> experiments)
	{
		this.experiments = experiments;
	}

	public String asMarkdownSource()
	{
		return experimentToMarkdown();
	}

	private String experimentToMarkdown()
	{
		var doc = new StringBuilder();

		doc.append(Markdown.heading("Simulation results", 1));

		for (Experiment experiment : experiments)
		{
			doc.append( Markdown.heading("Experiment - " + experiment.datasetContext.identifier(), 2).toString() )
				.append("\n");
			doc.append( datasetInfo(experiment) );
			doc.append( popularityInMarkdown(experiment.result().popularityMatrix) );
			doc.append( algoResultsInMarkdown(experiment.result().results) );
			doc.append( Markdown.rule() )
				.append("\n");
		}

		return doc.toString();
	}

	private StringBuffer datasetInfo(Experiment experiment)
	{
		var dataContext = experiment.datasetContext;
		var numAgents = dataContext.allAgents().count();
		var numProjects = dataContext.allProjects().count();
		var groupSize = dataContext.groupSizeConstraint();

		// slots...
		boolean allProjectsHaveSameAmountOfSlots = dataContext.allProjects().asCollection().stream().mapToInt(value -> value.slots().size()).distinct().count() == 1;
		Assert.that(allProjectsHaveSameAmountOfSlots).orThrowMessage("Not implemented: handling projects with heterogeneous amount of slots");
		var numSlots = dataContext.allProjects().asCollection().stream().mapToInt(value -> value.slots().size()).findAny().getAsInt();

		var doc = new StringBuffer();
		doc.append(Markdown.heading("Dataset info", 3).toString()).append("\n");
		doc.append(Markdown.image(embed(experiment.projectRankingDistribution.asChart()))).append("\n");
		doc.append(Markdown.unorderedList(
			"\\#agents: " + numAgents,
			"\\#projects: " + numProjects,
			"\\#slots per project: " + numSlots,
			"group sizes, min: " + groupSize.minSize() + ", max: " + groupSize.maxSize()
		)).append("\n");

		return doc;
	}

	private StringBuffer algoResultsInMarkdown(List<ExperimentAlgorithmSubresult> algoResults)
	{
		var doc = new StringBuffer();
		for (ExperimentAlgorithmSubresult algoResult : algoResults)
		{
			doc.append(algoResultInMarkdown(algoResult));
		}

		return doc;
	}

	private StringBuffer algoResultInMarkdown(ExperimentAlgorithmSubresult algoResult)
	{
		var doc = new StringBuffer();

		doc.append(Markdown.heading("Algorithm: " + algoResult.algo.name(), 3).toString()).append("\n");


		doc.append(Markdown.heading("Student perspective", 4)).append("\n");
		doc.append(Markdown.image(embed(algoResult.projectProfileCurveStudents.asChart()))).append("\n");
		doc.append(Markdown.unorderedList(
			"Gini: " + algoResult.giniStudentRanking.asDouble(),
			"AUPCR: " + algoResult.aupcrStudent.asDouble(),
			"Worst rank: " + algoResult.worstAssignedProjectRankOfStudents.asInt()
		)).append("\n");

		doc.append(Markdown.heading("Group perspective", 4)).append("\n");
//			doc.append(Markdown.image(embed(algoResult.projectProfileCurveGroup.asChart())) + "\n");
		doc.append(Markdown.unorderedList(
			"Gini: " + algoResult.giniGroupAggregateRanking.asDouble(),
			"AUPCR: " + algoResult.aupcrGroup.asDouble()
		)).append("\n");

		return doc;
	}

	private String popularityInMarkdown(PopularityMatrix popularityMatrix)
	{
		String doc = Markdown.heading("Algorithm popularity", 3) + "\n" +
			Markdown.text("A matching is more popular than some other, if more agents prefer it to the other - that is, they are better off.") + "\n" +
			Markdown.unorderedList((Object[]) popularityMatrix.asSet().toArray(Object[]::new)) + "\n";

		return doc;
	}

	private String embed(JFreeChart chart)
	{
		try {
			var data = ChartUtils.encodeAsPNG(chart.createBufferedImage(1000,800));
			return "data:image/png;base64," + new String(Base64.encodeBase64(data));


		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
