package nl.tudelft.aidm.optimalgroups.experiment.agp.report.profile;

import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
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
import java.util.Map;
import java.util.stream.Collectors;

public class RankProfileOfIndividualAndGroupingStudents
{
	private final Matching<Agent, Project> individuals;
	private final Matching<Agent, Project> pregrouping;
	
	public RankProfileOfIndividualAndGroupingStudents(Matching<Agent, Project> individuals, Matching<Agent, Project> pregrouping)
	{
		this.individuals = individuals;
		this.pregrouping = pregrouping;
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
	
	private Map<Integer, Long> ranks(Matching<Agent, Project> matching)
	{
		var ranks = AssignedRank.ProjectToStudent.inStudentMatching(matching)
			            .filter(assignedRank -> !assignedRank.isOfIndifferentAgent())
			            .map(assignedRank -> assignedRank.asInt().getAsInt())
			            .collect(Collectors.groupingBy(i -> i, Collectors.counting()));
		
		var maxRank = ranks.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
		// Range is categorical, so make sure all ranks are present for readability and easy comparability between charts
		for (int i = 1; i < maxRank; i++)
		{
			ranks.putIfAbsent(i, 0L);
		}
		
		return ranks;
	}
	
	private DefaultCategoryDataset dataset()
	{
		var dataset = new DefaultCategoryDataset();
		
		matchingToDataset(individuals, "Individual student", dataset);
		matchingToDataset(pregrouping, "Grouping student", dataset);
		
		return dataset;
	}
	
	private void matchingToDataset(Matching<Agent, Project> matching, String seriesName, DefaultCategoryDataset dataset)
	{
		var ranks = ranks(matching);
		ranks.keySet().stream().sorted().forEach(key -> {
			var value = ranks.get(key);
			dataset.addValue(value, seriesName, key);
		});
	}
}
