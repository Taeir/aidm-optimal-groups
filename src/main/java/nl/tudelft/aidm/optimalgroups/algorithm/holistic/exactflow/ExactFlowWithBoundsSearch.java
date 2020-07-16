package nl.tudelft.aidm.optimalgroups.algorithm.holistic.exactflow;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.dataset.projprefbinning.BinnableProjPref;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.lang.exception.ImplementMe;

import java.util.*;

public class ExactFlowWithBoundsSearch implements GroupToProjectMatching<Group.FormedGroup>
{
	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;
	private final DatasetContext datasetContext;

	public static void main(String[] args)
	{
		var ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		var thingy = new ExactFlowWithBoundsSearch(ce);
		var k = thingy.determineK();

		System.out.print(k);
	}

	public ExactFlowWithBoundsSearch(DatasetContext datasetContext)
	{
		this.agents = datasetContext.allAgents();
		this.projects = datasetContext.allProjects();
		this.groupSizeConstraint = datasetContext.groupSizeConstraint();
		this.datasetContext = datasetContext;
	}

	public int determineK()
	{
		List<Edge> edges = new ArrayList<>(agents.count() * projects.count());
		Map<Agent, List<Edge>> edgesFrom = new IdentityHashMap<>();
		Map<Project, List<Edge>> edgesTo = new IdentityHashMap<>();

		agents.asCollection().forEach(agent -> {
			agent.projectPreference().forEach((Project project, int rank) -> {
				var edge = new Edge(agent, project, rank);

				edges.add(edge);
				edgesFrom.computeIfAbsent(agent, __ -> new ArrayList<>()).add(edge);
				edgesTo.computeIfAbsent(project, __ -> new ArrayList<>()).add(edge);
			});
		});

		int k = 1;
		for (var agent : agents.asCollection()) {
			int l = projects.count();

			var prefs = agent.projectPreference().asListOfProjects();
			for (int i = prefs.size(); i --> 0;) {
				final var rank = i;
				var proj = prefs.get(rank);
				int count = (int) edgesTo.get(proj).stream()
								.filter(edge -> edge.rank <= rank)
								.count();
				if (count >= groupSizeConstraint.minSize() && rank < l) {
					l = rank;
				}
			}

			k = Math.max(l, k);
		}

		return k;
	}


	@Override
	public List<Match<Group.FormedGroup, Project>> asList()
	{
		throw new ImplementMe();
	}

	@Override
	public DatasetContext datasetContext()
	{
		throw new ImplementMe();
	}

	private static record Edge(Agent from, Project to, int rank){}
}
