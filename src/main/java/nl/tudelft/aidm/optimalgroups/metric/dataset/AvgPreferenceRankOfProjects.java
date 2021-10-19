package nl.tudelft.aidm.optimalgroups.metric.dataset;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.Statistics;

import java.util.*;
import java.util.stream.Collectors;

public class AvgPreferenceRankOfProjects
{
	private final List<ProjectPreference> prefProfiles;
	private final Projects allProjects;

	private AvgPreferenceRankOfProjects(List<ProjectPreference> prefProfiles, Projects allProjects)
	{
		this.prefProfiles = prefProfiles;
		this.allProjects = allProjects;
	}

	public static AvgPreferenceRankOfProjects fromAgents(Agents agents, Projects allProjects)
	{
		var preferencesOfAll = agents.asCollection().stream().map(Agent::projectPreference).collect(Collectors.toList());

		return new AvgPreferenceRankOfProjects(preferencesOfAll, allProjects);
	}

	public static AvgPreferenceRankOfProjects ofAgentsInDatasetContext(DatasetContext datasetContext)
	{
		return AvgPreferenceRankOfProjects.fromAgents(datasetContext.allAgents(), datasetContext.allProjects());
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

//	public void chartAsImage()
//	{
//		var chart = this.asChart();
//		var bufferedImage = chart.createBufferedImage(1000, 800);
//
//		bufferedImage.
//	}

	public JFreeChart asChart()
	{
		// Project -> List of ranks in profiles of students
		var projectToRanksMap = new HashMap<Project, List<Integer>>(allProjects.count(), 1);

		// init
		allProjects.forEach(project -> projectToRanksMap.put(project, new ArrayList<>(allProjects.count())));

		// collect rankings per project
		prefProfiles.forEach(profile -> {
			profile.forEach((project, rank, __) -> {
				// Oeffff
//				if (rank.unacceptable())
					// if unacceptible, do not include in the preferences visualization?
//					projectToRanksMap.get(project).add(Integer.MAX_VALUE);
				// Skip indifferent agents, they don't count because we can't ascertain how well they value their match
				
				if (rank.isPresent())
					projectToRanksMap.get(project).add(rank.asInt());
			});
		});

		var chartDataset = new DefaultBoxAndWhiskerCategoryDataset();

		// Convert data into JFreeChart's dataset - add the data sorted by the median rank of the project (from most to least)
		// Because JFreeChart's DefaultBoxAndWhiskerCategoryDataset renders the items in the order they are present in the chartDataset
		projectToRanksMap.forEach((project, ranks) -> Collections.sort(ranks)); // sort the ranks to get the median easily later
		projectToRanksMap.entrySet().stream()
			.sorted(
				Map.Entry.comparingByValue(
					Comparator.comparing((List<Integer> ranks) -> Statistics.calculateMedian(ranks, false))
						.thenComparing((List<Integer> ranks) -> Statistics.calculateMean(ranks)))
			)
			.forEach(entry ->
				chartDataset.add(entry.getValue(), "", String.valueOf(entry.getKey()))
			);

		var chart = ChartFactory.createBoxAndWhiskerChart("Avg rank of Project in preferences", "Project", "Avg rank", chartDataset, true);
		return chart;
	}
}
