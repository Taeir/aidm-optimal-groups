package nl.tudelft.aidm.optimalgroups.metric;

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
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.XYDataset;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectPreferencePairwiseVictories
{
	private final List<ProjectPreference> profileOfPreferences;
	private final Projects allProjects;

	private ProjectPreferencePairwiseVictories(List<ProjectPreference> profileOfPreferences, Projects allProjects)
	{
		this.profileOfPreferences = profileOfPreferences;
		this.allProjects = allProjects;
	}

	public static ProjectPreferencePairwiseVictories fromAgents(Agents agents, Projects allProjects)
	{
		var preferencesOfAll = agents.asCollection().stream().map(Agent::getProjectPreference).collect(Collectors.toList());

		return new ProjectPreferencePairwiseVictories(preferencesOfAll, allProjects);
	}

	public void drawAsChart() {
		// compute pairwise victories
		// note that the number of pairwise victories is the inverse of the rank of the project
		// that is, pairwisevictories_p1 = |projects| - rank_p1

		var numProjects = allProjects.count();
		var pairwiseVictories = new HashMap<Project, Integer>();

		for (ProjectPreference profileOfPreference : profileOfPreferences) {
			profileOfPreference.forEach(((Project project, int rank) ->
				pairwiseVictories.merge(project, rank, Integer::sum)
			));
		}

		var data = pairwiseVictories.entrySet().stream()
			.map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
			.sorted(Comparator.comparing(Pair::getValue))
			.collect(Collectors.toList());

		var chart = chart(data);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));

		ApplicationFrame chartFrame = new ApplicationFrame("Chart");
		chartFrame.setContentPane(chartPanel);
		chartFrame.pack();
		chartFrame.setVisible(true);
	}

	JFreeChart chart(List<Pair<Project, Integer>> data)
	{
		var dataset = new DefaultCategoryDataset();

		data.forEach(xyPair -> {
			dataset.addValue(xyPair.getValue() / (double) profileOfPreferences.size(), "", String.valueOf(xyPair.getKey().id()));
		});

		JFreeChart barChart = ChartFactory.createLineChart("Project preference pairwise victories", "Project", "# Pairwise victories", dataset);
		return barChart;
	}
}
