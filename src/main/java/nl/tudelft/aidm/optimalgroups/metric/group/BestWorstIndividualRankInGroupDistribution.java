package nl.tudelft.aidm.optimalgroups.metric.group;

import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Collection;
import java.util.stream.Collectors;

public class BestWorstIndividualRankInGroupDistribution
{
	private final Collection<? extends Group> groups;

	public BestWorstIndividualRankInGroupDistribution(Collection<? extends Group> groups)
	{
		this.groups = groups;
	}

	public JFreeChart asChart()
	{
		var data = groups.stream()
			.flatMapToInt(group -> new BestWorstIndividualRankAttainableInGroup(group).asInt().stream())
			.boxed()
			.collect(Collectors.groupingBy(integer -> integer, Collectors.counting()));

		XYSeries series = new XYSeries("");
		data.forEach(series::add);

		var chart = ChartFactory.createXYBarChart(
			"Distribution of Best-Worst-Invididual-Rank-In-Group ranks", "Best worst individual rank", false, "#Groups", new XYSeriesCollection(series));

		NumberAxis numberAxis = new NumberAxis();
		numberAxis.setTickUnit(new NumberTickUnit(1));
		numberAxis.setAutoRangeIncludesZero(false);


		XYPlot plot = (XYPlot) chart.getPlot();
//        plot.getRenderer().setSeriesPaint(0, Color.RED);
//        plot.getRenderer().setSeriesFillPaint(0, Color.RED);
//        plot.getRenderer().setSeriesStroke(0, new BasicStroke());
//        plot.getRenderer().setSeries(0, new BasicStroke());

		StandardXYBarPainter painter = new StandardXYBarPainter();
		((XYBarRenderer) plot.getRenderer()).setBarPainter(painter);
		plot.getRenderer().setSeriesOutlinePaint(0, Color.BLACK);
		plot.getRenderer().setSeriesOutlineStroke(0, new BasicStroke(5));

//        plot.setBackgroundPaint(ChartColor.LIGHT_RED);
		plot.setDomainAxis(numberAxis);
//        plot.set

		return chart;
	}
}
