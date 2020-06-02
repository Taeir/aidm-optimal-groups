package nl.tudelft.aidm.optimalgroups.experiment.agp.report;

import net.steppschuh.markdowngenerator.Markdown;
import nl.tudelft.aidm.optimalgroups.experiment.BinnedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.experiment.agp.Experiment;
import nl.tudelft.aidm.optimalgroups.experiment.agp.ExperimentAlgorithmSubresult;
import nl.tudelft.aidm.optimalgroups.experiment.agp.ExperimentResult;
import nl.tudelft.aidm.optimalgroups.metric.group.LeastWorstIndividualRankInGroupDistribution;
import nl.tudelft.aidm.optimalgroups.metric.matching.NumberAgentsMatched;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import plouchtch.assertion.Assert;

import java.util.List;
import java.util.stream.Collectors;

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
		doc.append("\n");

		for (Experiment experiment : experiments)
		{
			doc.append( Markdown.heading("Experiment - " + experiment.datasetContext.identifier(), 2).toString() )
				.append("\n");
			doc.append( datasetInfo(experiment) );
			doc.append( summary(experiment.result()) );
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

		doc.append(Markdown.unorderedList(
			"\\#agents: " + numAgents,
			"\\#projects: " + numProjects,
			"\\#slots per project: " + numSlots,
			"group sizes, min: " + groupSize.minSize() + ", max: " + groupSize.maxSize()
		)).append("\n");

		JFreeChart distribProjectsInPreferencesChart = experiment.projectRankingDistribution.asChart();
		doc.append(Markdown.image(embed(distribProjectsInPreferencesChart))).append("\n\n");

		var binnedProjectPreferences = BinnedProjectPreferences.exactTopRanksBins(dataContext, 3, 30);
		doc.append(binnedProjectPreferences.asMarkdownTable()).append("\n");


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

		doc.append(Markdown.heading("Algorithm: " + algoResult.algo().name(), 3).toString()).append("\n");


		doc.append(Markdown.heading("Student perspective", 4)).append("\n");

		var numStudentsMatched = NumberAgentsMatched.fromGroupMatching(algoResult.producedMatching()).asInt();
		int numStudentsInDataset = algoResult.producedMatching().datasetContext().allAgents().count();
		doc.append(Markdown.text(String.format("Number of students matched: %s (out of: %s)\n\n", numStudentsMatched, numStudentsInDataset)));

		var rankDistribution = algoResult.studentPerspectiveMetrics.rankDistribution().asChart();
		doc.append(Markdown.image(embed(rankDistribution))).append("\n\n");

		var groups = algoResult.producedMatching().asList().stream().map(Match::from).collect(Collectors.toList());
		var bestWorstIndividualRankInGroupDistribution = new LeastWorstIndividualRankInGroupDistribution(groups).asChart();
		doc.append(Markdown.image(embed(bestWorstIndividualRankInGroupDistribution))).append("\n\n");

		doc.append(Markdown.unorderedList(
			"Gini: " + algoResult.studentPerspectiveMetrics.giniCoefficient().asDouble(),
			"AUPCR: " + algoResult.studentPerspectiveMetrics.aupcr().asDouble(),
			"Worst rank: " + algoResult.studentPerspectiveMetrics.worstRank().asInt()
		)).append("\n");


		doc.append(Markdown.heading("Group perspective", 4)).append("\n");
//			doc.append(Markdown.image(embed(algoResult.projectProfileCurveGroup.asChart())) + "\n");
		doc.append(Markdown.unorderedList(
			"Gini: " + algoResult.groupPerspectiveMetrics.giniCoefficient().asDouble(),
			"AUPCR: " + algoResult.groupPerspectiveMetrics.aupcr().asDouble()
		)).append("\n");

		return doc;
	}

	private String summary(ExperimentResult experimentResult)
	{
		String doc = Markdown.heading("Summary of results", 3) + "\n";

		 doc += Markdown.heading("Algorithm popularity", 4) + "\n" +
			 Markdown.italic("Algorithm name followed by the number of agents, in braces, that prefer it over the other") + "\n" +
			Markdown.unorderedList((Object[]) experimentResult.popularityMatrix.asSet().toArray(Object[]::new)) + "\n";

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
