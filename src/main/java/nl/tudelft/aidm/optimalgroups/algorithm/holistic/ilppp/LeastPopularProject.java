package nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp;

import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMaxFlowMatching;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.profilecurve.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LeastPopularProject implements Project
{
	private final DatasetContext datasetContext;
	private final Map<Project, List<Agent>> grouping;
	private Project theLeastPopularProject = null;

	public LeastPopularProject(DatasetContext datasetContext, Map<Project, List<Agent>> grouping)
	{
		this.datasetContext = datasetContext;
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

		projects.asCollection()/*.parallelStream()*/.forEach(project -> {
			var projectsWithoutOne = projects.without(project);

			// TODO: include this maxflow result?
			var matchingWithoutGivenProject = new StudentProjectMaxFlowMatching(datasetContext, students, projectsWithoutOne);

//			SingleGroupPerProjectMatching matching = new SingleGroupPerProjectMatching(matchingWithoutGivenProject);

			// calc effect
			var metric = new AUPCRStudent(matchingWithoutGivenProject, projectsWithoutOne, students);

			double result = metric.asDouble();
			results.put(project, metric);
		});

		// The "Least Popular" project is the one whose removal results in highest metric (AUPCR) relative to any other
		// now we just need to determine which project that is, to do so we need to sort the tuples computed above by the value (AUPCR result)
		// by sorting descending, the least popular project (key) is the top most (0th) element
		ArrayList<Map.Entry<Project, AUPCR>> entries = new ArrayList<>(results.entrySet());
		entries.sort(Comparator.comparing((Map.Entry<Project, AUPCR> entry) -> entry.getValue().asDouble()).reversed());

		// some dbg / progess report
//		for (Map.Entry<Project, AUPCR> entry : entries)
//		{
//			System.out.printf("\tExcluding project '%s' with %s students has effect: %s\n", entry.getKey(), grouping.get(entry.getKey()).size(), entry.getValue().result());
//		}

		var leastPopular = entries.get(0);
//		System.out.printf("Removing project '%s' has least effect on AUPCR (resulting in: %s)\n", leastPopular.getKey(), leastPopular.getValue().result());

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
