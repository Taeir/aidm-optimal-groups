package nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankGroup;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class ProjectProfileCurveGroup extends ProfileCurveOfMatching
{

    public ProjectProfileCurveGroup(Matching<Group.FormedGroup, Project> matching) {
        super(matching);
    }

    @Override
    void calculate()
    {
        if (this.profile == null)
        {
            this.profile = new HashMap<>();
            for (Match<Group.FormedGroup, Project> match : this.matching.asList())
            {
                AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);
                int groupRank = assignedProjectRank.groupRank();

                this.worstRank = Math.max(this.worstRank, groupRank);

                if (this.profile.containsKey(groupRank))
                    this.profile.put(groupRank, this.profile.get(groupRank) + 1);
                else
                    this.profile.put(groupRank, 1);

            }
        }
    }

    @Override
    public void printResult(PrintStream printStream) {
        printStream.println("Group project profile results:");
        for (Map.Entry<Integer, Integer> entry : this.asMap().entrySet()) {
            printStream.printf("\t- Rank %d: %d group(s)\n", entry.getKey(), entry.getValue());
        }
        printStream.printf("\t- Cumulative rank of groups: %d\n\n", this.cumulativeRanks());
    }

    public JFreeChart asChart()
    {
        this.calculate();

        XYSeries series = new XYSeries("Profile curve - group aggregate preference");
        this.profile.forEach(series::add);

        var chart = ChartFactory.createXYStepAreaChart("Profile curve - group aggregate preference", "Rank", "#Groups", new XYSeriesCollection(series));
        return chart;
    }

	public void displayChart()
	{
        var chart = this.asChart();

        /* Output stuff */
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));

        ApplicationFrame chartFrame = new ApplicationFrame(chart.getTitle().getText());
        chartFrame.setContentPane(chartPanel);
        chartFrame.pack();
        chartFrame.setVisible(true);
	}
}
