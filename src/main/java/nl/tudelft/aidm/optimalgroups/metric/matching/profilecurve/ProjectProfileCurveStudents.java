package nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve;

import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankGroup;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class ProjectProfileCurveStudents extends ProfileCurveOfMatching
{

    public ProjectProfileCurveStudents(Matching<Group.FormedGroup, Project> matching) {
        super(matching);
    }

    @Override
    void calculate()
    {
        if (profile == null) {
            this.profile = new HashMap<>();
            for (Match<Group.FormedGroup, Project> match : this.matching.asList()) {
                AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);

                assignedProjectRank.studentRanks().forEach(metric -> {
                    int studentsRank = metric.studentsRank();

                        // Student rank -1 indicates no project preference, hence we exclude
                        // in order to not inflate our performance
                    if (studentsRank == -1)
                        return;

                    this.worstRank = Math.max(this.worstRank, studentsRank);

                    if (this.profile.containsKey(studentsRank))
                        this.profile.put(studentsRank, this.profile.get(studentsRank) + 1);
                    else
                        this.profile.put(studentsRank, 1);
                });
            }
        }
    }

    public void displayChart()
    {
        calculate();

        XYSeries series = new XYSeries("Profile curve - student");
        this.profile.forEach(series::add);

        var chart = ChartFactory.createXYStepAreaChart("Profile curve - students", "Rank", "#Students", new XYSeriesCollection(series));

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
        printStream.printf("\t- Cumulative rank of students: %d\n\n", this.cumulativeRanks());
    }
}
