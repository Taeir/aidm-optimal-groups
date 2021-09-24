package nl.tudelft.aidm.optimalgroups.experiment.agp.report.profile;

import net.steppschuh.markdowngenerator.table.Table;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
public class RankProfile_SinglePregroupingSatisfiedUnsatisfied
{
	private final Matching<Agent, Project> single;
	private final Matching<Agent, Project> satisfiedPregrouping;
	private final Matching<Agent, Project> unsatisfiedPregrouping;
	
	public RankProfile_SinglePregroupingSatisfiedUnsatisfied(Matching<Agent, Project> single, Matching<Agent, Project> satisfiedPregrouping, Matching<Agent, Project> unsatisfiedPregrouping)
	{
		this.single = single;
		this.satisfiedPregrouping = satisfiedPregrouping;
		this.unsatisfiedPregrouping = unsatisfiedPregrouping;
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
	
	public Table asTable()
	{
		var series = new ArrayList<>(3);
		
		series.add( RankProfileTable.SeriesData.from(single, "'Single' student") );
		series.add( RankProfileTable.SeriesData.from(satisfiedPregrouping, "Satisfied 'pregrouping' student") );
		series.add( RankProfileTable.SeriesData.from(unsatisfiedPregrouping, "Unsatisfied 'pregrouping' student") );
		
		return new RankProfileTable(series.toArray(RankProfileTable.SeriesData[]::new)).asMarkdownTable();
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
		
		matchingToDataset(single, "'Single' student", dataset);
		matchingToDataset(satisfiedPregrouping, "Satisfied 'pregrouping' student", dataset);
		matchingToDataset(unsatisfiedPregrouping, "Unsatisfied 'pregrouping' student", dataset);
		
		return dataset;
	}
	
	private void matchingToDataset(Matching<Agent, Project> matching, String seriesName, DefaultCategoryDataset dataset)
	{
		var ranks = ranks(matching);
		ranks.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
			var key = entry.getKey();
			var value = entry.getValue();
			dataset.addValue(value, seriesName, key);
		});
	}
	
	private Map<Integer, Long> ranks(Matching<Agent, Project> matching)
	{
		var ranks = AssignedRank.ProjectToStudent.inStudentMatching(matching)
			            .filter(assignedRank -> !assignedRank.isOfIndifferentAgent())
			            .map(assignedRank -> assignedRank.asInt().getAsInt())
			            .collect(Collectors.groupingBy(i -> i, Collectors.counting()));
		
		var maxRank = ranks.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
		// Range is categorical, so make sure all ranks are present for readability and easy comparability between charts
		for (int i = 1; i <= maxRank; i++)
		{
			ranks.putIfAbsent(i, 0L);
		}
		
		return ranks;
	}
}
