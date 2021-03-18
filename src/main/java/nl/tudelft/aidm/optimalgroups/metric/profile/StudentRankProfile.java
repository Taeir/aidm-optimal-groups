package nl.tudelft.aidm.optimalgroups.metric.profile;

import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.*;

import java.awt.*;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class StudentRankProfile extends AbstractRankProfile
{

    private final Matching<Agent, Project> matching;

    public StudentRankProfile(Matching<Agent, Project> matching) {
        this.matching = matching;
    }

    @Override
    protected void calculate()
    {
        if (profile == null) {

            this.profile = new HashMap<>();

            for (var match : this.matching.asList()) {
                var assignedProjectRank = new AssignedRank.ProjectToStudent(match);

                // Student rank -1 indicates no project preference, hence we exclude
                // in order to not inflate our performance
                if (assignedProjectRank.isOfIndifferentAgent())
                    continue;

                int rank = assignedProjectRank.asInt().orElseThrow();
                this.worstRank = Math.max(this.worstRank, rank);
                this.profile.merge(rank, 1, Integer::sum);
            }
        }
    }

    public JFreeChart asChart(String titleHint)
    {
        calculate();

        XYSeries series = new XYSeries("");
        this.profile.forEach(series::add);

        var chart = ChartFactory.createXYBarChart(
            "Assigned rank profile (individual) - " + titleHint, "Rank", false, "#Students", new XYSeriesCollection(series));

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

    public void displayChart(String titleHint)
    {
        var chart = this.asChart(titleHint);

        /* Output stuff */
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));

        ApplicationFrame chartFrame = new ApplicationFrame(chart.getTitle().getText());
        chartFrame.setContentPane(chartPanel);
        chartFrame.pack();
        chartFrame.setVisible(true);
    }

    @Override
    public void printResult(PrintStream printStream) {
        printStream.println("Student project profile results:");
        for (Map.Entry<Integer, Integer> entry : this.asMap().entrySet()) {
            printStream.printf("\t- Rank %d: %d student(s)\n", entry.getKey(), entry.getValue());
        }
        printStream.printf("\t- Cumulative rank of students: %d\n\n", this.sumOfRanks());
    }
}
