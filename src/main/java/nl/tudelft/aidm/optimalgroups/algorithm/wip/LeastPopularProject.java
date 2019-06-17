package nl.tudelft.aidm.optimalgroups.algorithm.wip;

import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMaxFlow;
import nl.tudelft.aidm.optimalgroups.metric.AUPCR;
import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Agents;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;
import nl.tudelft.aidm.optimalgroups.model.entity.Projects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LeastPopularProject implements Project
{
	private final Map<Project, List<Agent>> grouping;
	private Project theLeastPopularProject = null;

	public LeastPopularProject(Map<Project, List<Agent>> grouping)
	{
		this.grouping = grouping;
	}

	@Override
	public String name()
	{
		return getOrDetermine().name();
	}

	@Override
	public int id()
	{
		return getOrDetermine().id();
	}

	@Override
	public List<ProjectSlot> slots()
	{
		return getOrDetermine().slots();
	}

	private Project getOrDetermine()
	{
		if (theLeastPopularProject == null)
			theLeastPopularProject = determine();

		return theLeastPopularProject;
	}

	// most naive implementation: just maxflow it and
	private Project determine()
	{
		// Collect all students
		var students = Agents.from(grouping.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));

		// All projects that have students assigned (the filter is probably redundant but it costs nothing to do)
		var projects = Projects.from(
			grouping.keySet().stream()
				.filter(project -> grouping.get(project).size() > 0)
				.collect(Collectors.toList())
		);

		// mapping: AUPCR scores when Project is exluded
		var results = new ConcurrentHashMap<Project, AUPCR>(projects.count());

		projects.asCollection().parallelStream().forEach(project -> {
			var projectsWithoutThisOne = projects.without(project);

			// TODO: include this maxflow result?
			StudentProjectMaxFlow maxflowResultWithoutCurrentProject = StudentProjectMaxFlow.of(students, projectsWithoutThisOne);

			// calc effect
			var metric = new AUPCR.StudentAUPCR(maxflowResultWithoutCurrentProject.result(), projectsWithoutThisOne, students);

			results.put(project, metric);
		});

		ArrayList<Map.Entry<Project, AUPCR>> entries = new ArrayList<>(results.entrySet());
		// sort from Highest to lowest AUPCR, we want to remove project whose removal gives highest AUPCR (that is: projects whose removal has least impact in AUPCR)
		entries.sort(Comparator.comparing((Map.Entry<Project, AUPCR> entry) -> entry.getValue().result()).reversed());
		for (Map.Entry<Project, AUPCR> entry : entries)
		{
			// some dbg / progess report
			System.out.printf("Excluding project '%s' with %s students has effect: %s\n", entry.getKey(), grouping.get(entry.getKey()).size(), entry.getValue().result());
		}

		var leastPopular = entries.get(0);
		System.out.printf("Project '%s' has least AUPCR (%s)\n", leastPopular.getKey(), leastPopular.getValue().result());

		return leastPopular.getKey();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof LeastPopularProject)
		{
			LeastPopularProject other = (LeastPopularProject) obj;

			obj = ((LeastPopularProject) obj).getOrDetermine();
		}

		return getOrDetermine().equals(obj);
	}

}
