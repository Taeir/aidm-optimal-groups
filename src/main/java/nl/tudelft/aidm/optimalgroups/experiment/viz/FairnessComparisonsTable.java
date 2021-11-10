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

import java.util.*;
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
	
	private static String bold(String text)
	{
		return "**" + text + "**";
	}
	
	private static String conditional(String textTrue, String textFalse, boolean condition)
	{
		if (condition)
			return textTrue;
		else
			return textFalse;
	}
	
	static final class ComparisonOutcome
	{
		private final Matching<Agent, Project> fairness;
		private final Matching<Agent, Project> vanilla;
		
		private final Map<Matching, SumOfRanks> sumOfRanks;
		private final Map<Matching, WorstAssignedRank> worstRank;
		
		public ComparisonOutcome(Matching<Agent, Project> fairness, Matching<Agent, Project> vanilla)
		{
			this.fairness = fairness;
			this.vanilla = vanilla;
			
			this.sumOfRanks = new IdentityHashMap<>();
			this.worstRank = new IdentityHashMap<>();
			
			for (var x : List.of(fairness, vanilla))
			{
				sumOfRanks.put(x, new SumOfRanks(x));
				worstRank.put(x, new WorstAssignedRank.ProjectToStudents(x));
			}
		}
		
		public String profileDeltaFairnessVsVanilla()
		{
			final var profileFairness = Profile.of(fairness);
			final var profileVanilla = Profile.of(vanilla);
			var paretoOutcome = new ParetoComperator().compare(profileFairness, profileVanilla);
			
			return switch (paretoOutcome) {
				case BETTER, WORSE, NONE -> profileFairness.subtracted(profileVanilla).toString();
				case SAME -> Profile.fromProfileArray().toString(); // empty
			};
		}
		
		public String fairnessOutcomeAsString()
		{
			return cellTextOutcome(fairness, vanilla);
		}
		
		public String vanillaOutcomeAsString()
		{
			return cellTextOutcome(vanilla, fairness);
		}
	
		public String cellTextOutcome(Matching<Agent, Project> thisOutcome, Matching<Agent, Project> thatOutcome)
		{
			var profileThis = Profile.of(thisOutcome);
			var profileThat = Profile.of(thatOutcome);
			
			var paretoOutcome = new ParetoComperator().compare(profileThis, profileThat);
			
			var thisSOR = new SumOfRanks(thisOutcome);
			var thatSOR = new SumOfRanks(thatOutcome);
			
			final var worstRankThis = WorstAssignedRank.ProjectToStudents.in(thisOutcome).asInt().intValue();
			final var worstRankThat = WorstAssignedRank.ProjectToStudents.in(thatOutcome).asInt().intValue();
			
			return switch (paretoOutcome) {
				case BETTER -> asStringIfParetoBetter(thisOutcome, thatOutcome);
				case SAME -> asStringIfParetoSame();
				case WORSE -> asStringIfParetoWorse(thisOutcome, thatOutcome);
				case NONE -> asStringIfParetoNone(thisOutcome, thatOutcome);
			};
		}
		
		private boolean isWorstRankSame(Matching<Agent, Project> thisM, Matching<Agent, Project> thatM)
		{
			return this.worstRank.get(thisM)
					.compareTo(this.worstRank.get(thatM)) == 0;
		}
		
		private String asStringIfParetoBetter(Matching<Agent, Project> betterM, Matching<Agent, Project> worseM)
		{
			var worstRank = isWorstRankSame(betterM, worseM)
					? ""
                    : " [%s]".formatted(this.worstRank.get(betterM).asInt());
			
			var sumOfRanksDelta = this.sumOfRanks.get(betterM).asInt() - this.sumOfRanks.get(worseM).asInt();
//			var deltaPercent = 1.0 * this.sumOfRanks.get(betterM).asInt() / this.sumOfRanks.get(worseM).asInt() * 100 - 100;
			
//			return bold("Δ %s (%+.1f%%)" + worstRank).formatted(sumOfRanksDelta, deltaPercent);
			
			
			return bold("Δ%s (%s)" + worstRank).formatted(sumOfRanksDelta, this.sumOfRanks.get(betterM).asInt());
		}
		
		private String asStringIfParetoNone(Matching<Agent, Project> thisM, Matching<Agent, Project> thatM)
		{
			var worstRank = isWorstRankSame(thisM, thatM)
					? ""
                    : " [%s]".formatted(this.worstRank.get(thisM).asInt());
			
			var sumOfRanksDelta = this.sumOfRanks.get(thisM).asInt() - this.sumOfRanks.get(thatM).asInt();
			
//			var deltaPercent = 1.0 * this.sumOfRanks.get(thisM).asInt() / this.sumOfRanks.get(thatM).asInt() * 100 - 100;
//			return "%s (%+.1f%%)%s".formatted(this.sumOfRanks.get(thisM).asInt(), deltaPercent, worstRank);
			
			return "%s (Δ%s)%s".formatted(this.sumOfRanks.get(thisM).asInt(), sumOfRanksDelta, worstRank);
		}
		
		private String asStringIfParetoWorse(Matching<Agent, Project> worseM, Matching<Agent, Project> betterM)
		{
			var worstRank = isWorstRankSame(worseM, betterM)
					? ""
					: "[%s]".formatted(this.worstRank.get(worseM).asInt());
			
			return worstRank;
		}
		
		private String asStringIfParetoSame()
		{
			return "=";
		}
		
	}
	
	public Table asMarkdownTable()
	{
		var rows = new ArrayList<List<String>>();
		
		var header = List.of("Dataset", "Fairness", "Vanilla", "Profile delta (Fairness - Vanilla)");
		rows.add(header);
		
		for (Result result : this.results)
		{
			var comparisonOutcome = new ComparisonOutcome(result.fairnessOutcome(), result.vanillaOutcome());
			
//			var deltaOfWinner = switch (paretoOutcome) {
//				case FAIRNESS -> profileFairness.subtracted(profileVanilla);
//				case VANILLA -> profileVanilla.subtracted(profileFairness);
//				case NONE -> fairnessSOR.asInt() < vanillaSOR.asInt()
//						? profileFairness.subtracted(profileVanilla)
//						: profileVanilla.subtracted(profileFairness);
//				case EQUAL -> Profile.fromProfileArray(0); // empty
//			};
			
			var row = List.of(
					result.datasetContext().identifier(),
					comparisonOutcome.fairnessOutcomeAsString(),
					comparisonOutcome.vanillaOutcomeAsString(),
					comparisonOutcome.profileDeltaFairnessVsVanilla()
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
