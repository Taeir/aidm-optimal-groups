package nl.tudelft.aidm.optimalgroups.metric.dataset;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.apache.commons.math3.util.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
		var preferencesOfAll = agents.asCollection().stream().map(Agent::getProjectPreference).collect(Collectors.toList());

		return new AvgPreferenceRankOfProjects(preferencesOfAll, allProjects);
	}

	public void displayChart()
	{
		var chartDataset = new DefaultBoxAndWhiskerCategoryDataset();

		// Project -> List of ranks in profiles of students
		var projectToRanksMap = new HashMap<Project, List<Integer>>(allProjects.count(), 1);

		// init
		allProjects.forEach(project -> projectToRanksMap.put(project, new ArrayList<>(allProjects.count())));

		// collect rankings per project
		prefProfiles.forEach(profile -> {
			profile.forEach((Project project, int rank) -> {
				projectToRanksMap.get(project).add(rank);
			});
		});

		// Convert data into JFreeChart's dataset - add the data sorted by the avg rank of the project (most wanted proj first)
		// Because JFreeChart's DefaultBoxAndWhiskerCategoryDataset renders the items in the order they are present in the chartDataset
		var dataToSort = new ArrayList<>(projectToRanksMap.entrySet());
		dataToSort.stream()
			// Not efficient, but was easy to write, it works and seems fast enough
			.sorted(Comparator.comparing(ranksOfProject -> ranksOfProject.getValue().stream().mapToInt(Integer::intValue).sum()))
			.forEach(entry ->
				chartDataset.add(entry.getValue(), "", String.valueOf(entry.getKey()))
			);

		var chart = ChartFactory.createBoxAndWhiskerChart("Avg rank of Project in preferences", "Project", "Avg rank", chartDataset, true);

		/* Output stuff */
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));

		ApplicationFrame chartFrame = new ApplicationFrame("Chart");
		chartFrame.setContentPane(chartPanel);
		chartFrame.pack();
		chartFrame.setVisible(true);
	}
}
