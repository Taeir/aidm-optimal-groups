package nl.tudelft.aidm.optimalgroups.experiment.agp.report.profile;

import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
import nl.tudelft.aidm.optimalgroups.model.Profile;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
public class RankProfileGraph
{
	public record NamedRankProfile(Profile profile, String name) {}
	
	private final NamedRankProfile[] namedRankProfiles;
//
//	private final Matching<Agent, Project> single;
//	private final Matching<Agent, Project> pregrouping;
	
	public RankProfileGraph(NamedRankProfile... namedRankProfiles)
	{
		this.namedRankProfiles = namedRankProfiles;
//		this.single = single;
//		this.pregrouping = pregrouping;
	}
	
	public JFreeChart asChart(String titleHint)
	{
		var dataset = dataset();
		
		var chart = ChartFactory.createStackedBarChart(
			"Profile matching - " + titleHint,
			"Rank",
			"# Students",
			dataset,
			PlotOrientation.VERTICAL,
			true,
			false,
			false
		);
		
		var plot = (CategoryPlot) chart.getPlot();
		BarPainter painter = new StandardBarPainter();
		
		((StackedBarRenderer) plot.getRenderer()).setBarPainter(painter);
		plot.getRenderer().setSeriesOutlinePaint(0, Color.BLACK);
		plot.getRenderer().setSeriesOutlineStroke(0, new BasicStroke(5));
		
		
		return chart;
	}
	
	public void displayChart(String titleHint)
	{
		var chart = this.asChart(titleHint);
		
		/* Output stuff */
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(1000, 800));
		
		ApplicationFrame chartFrame = new ApplicationFrame(chart.getTitle().getText());
		chartFrame.setContentPane(chartPanel);
		chartFrame.pack();
		chartFrame.setVisible(true);
	}
	
	private DefaultCategoryDataset dataset()
	{
		var dataset = new DefaultCategoryDataset();
		
		for (NamedRankProfile namedRankProfile : namedRankProfiles)
		{
			namedRankProfile.profile().forEach((rank, count) -> {
				dataset.addValue((Number) count, namedRankProfile.name(), rank);
			});
		}
		
		return dataset;
	}
	
	public RankProfileTable asTable()
	{
		return new RankProfileTable(
				Arrays.stream(this.namedRankProfiles)
						.map(namedRankProfile -> new RankProfileTable.SeriesData(namedRankProfile.profile(), namedRankProfile.name()))
						.toArray(RankProfileTable.SeriesData[]::new)
		);
	}
	
	private Map<Integer, Long> ranks(Matching<Agent, Project> matching)
	{
		var ranks = AssignedRank.ProjectToStudent.inStudentMatching(matching)
			            .filter(assignedRank -> !assignedRank.isOfIndifferentAgent())
			            .map(assignedRank -> assignedRank.asInt().getAsInt())
			            .collect(Collectors.groupingBy(i -> i, Collectors.counting()));
		
		var maxRank = ranks.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
		
		// The chart range (x-axis) is set to be categorical, ensure all ranks are present even if empty
		// Note, don't have to do maxRank as it is guaranteed to be present
		for (int i = 1; i < maxRank; i++)
		{
			ranks.putIfAbsent(i, 0L);
		}
		
		return ranks;
	}
}
