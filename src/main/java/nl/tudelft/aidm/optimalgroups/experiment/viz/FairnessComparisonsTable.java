package nl.tudelft.aidm.optimalgroups.experiment.viz;

import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.table.TableRow;
import nl.tudelft.aidm.optimalgroups.metric.rank.SumOfRanks;
import nl.tudelft.aidm.optimalgroups.metric.rank.WorstAssignedRank;
import nl.tudelft.aidm.optimalgroups.model.Profile;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.comparison.ParetoComperator;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FairnessComparisonsTable
{
	private final Collection<Result> results;
	
	public FairnessComparisonsTable(Collection<Result> results)
	{
		this.results = results;
	}
	
	//////////////////////
	
//	public JFreeChart asChart()
//	{
//		var dataset = dataset();
//
//		var chart = ChartFactory.createStackedBarChart(
//			"Profile matching - ",
//			"Rank",
//			"# Students",
//			dataset,
//			PlotOrientation.VERTICAL,
//			true,
//			false,
//			false
//		);
//
//		var plot = (CategoryPlot) chart.getPlot();
//		BarPainter painter = new StandardBarPainter();
//
//		((StackedBarRenderer) plot.getRenderer()).setBarPainter(painter);
//		plot.getRenderer().setSeriesOutlinePaint(0, Color.BLACK);
//		plot.getRenderer().setSeriesOutlineStroke(0, new BasicStroke(5));
//
//
//		return chart;
//	}
//
//	public void displayChart(String titleHint)
//	{
//		var chart = this.asChart(titleHint);
//
//		/* Output stuff */
//		ChartPanel chartPanel = new ChartPanel(chart);
//		chartPanel.setPreferredSize(new Dimension(1000, 800));
//
//		ApplicationFrame chartFrame = new ApplicationFrame(chart.getTitle().getText());
//		chartFrame.setContentPane(chartPanel);
//		chartFrame.pack();
//		chartFrame.setVisible(true);
//	}
	
	private String bold(String text)
	{
		return "**" + text + "**";
	}
	
	public Table asMarkdownTable()
	{
		var rows = new ArrayList<List<String>>();
		
		var header = List.of("Dataset", "Fairness", "Vanilla");
		rows.add(header);
		
		for (Result result : this.results)
		{
			var paretoOutcome = result.paretoComparisonFairnessVsVanilla();
			
			var fairnessAgentToProjectView = result.fairnessOutcome();
			var vanillaAgentToPorjectView = result.vanillaOutcome();
			
			var fairnessSumOfRanks = new SumOfRanks(fairnessAgentToProjectView);
			var vanillaSumOfRanks = new SumOfRanks(vanillaAgentToPorjectView);
			
			var fairnessOutcome = switch (paretoOutcome) {
				case FAIRNESS -> bold("Δ %s".formatted(fairnessSumOfRanks.asInt() - vanillaSumOfRanks.asInt()));
				case EQUAL -> "=";
				case VANILLA -> "";
				case NONE -> "%s (%s)".formatted(fairnessSumOfRanks.asInt(), WorstAssignedRank.ProjectToStudents.in(fairnessAgentToProjectView).asInt());
			};
			
			var vanillaOutcome = switch (paretoOutcome) {
				case VANILLA -> bold("Δ %s".formatted(vanillaSumOfRanks.asInt() - fairnessSumOfRanks.asInt()));
				case EQUAL -> "=";
				case FAIRNESS -> "";
				case NONE -> "%s (%s)".formatted(vanillaSumOfRanks.asInt(), WorstAssignedRank.ProjectToStudents.in(vanillaAgentToPorjectView).asInt());
			};
			
			var row = List.of(
					result.datasetContext().identifier(),
					fairnessOutcome,
					vanillaOutcome
			);
			
			rows.add(row);
		}
		
		return rows.stream()
				.map(row -> new TableRow(row))
				.collect(Collectors.collectingAndThen(Collectors.toList(), Table::new));
	}
	
	//////////////////////
	
	
	public record Result(
			Matching<Agent, Project> fairnessOutcome,
			Matching<Agent, Project> vanillaOutcome
	) {
		public Result
		{
			Assert.that(fairnessOutcome.datasetContext().equals(vanillaOutcome.datasetContext()))
					.orThrowMessage("Dataset mismatch");
		}
		
		public DatasetContext datasetContext()
		{
			return fairnessOutcome.datasetContext();
		}
		
		public enum ParetoComparisonOutcome
		{
			FAIRNESS,
			VANILLA,
			EQUAL,
			NONE
		}
		
		public ParetoComparisonOutcome paretoComparisonFairnessVsVanilla()
		{
			var fairnessProfile = Profile.of(fairnessOutcome);
			var vanillaProfile = Profile.of(vanillaOutcome);
			
			var outcome = new ParetoComperator().compare(fairnessProfile, vanillaProfile);
			
			return switch (outcome) {
				case BETTER -> ParetoComparisonOutcome.FAIRNESS;
				case WORSE -> ParetoComparisonOutcome.VANILLA;
				case SAME -> ParetoComparisonOutcome.EQUAL;
				case NONE -> ParetoComparisonOutcome.NONE;
			};
		}
	}
}
