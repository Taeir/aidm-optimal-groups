package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.exactflow.ExactFlowWithBoundsSearch;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.metric.rank.WorstAssignedRank;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.search.DynamicSearch;
import nl.tudelft.aidm.optimalgroups.search.Solution;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class Pessimistic extends DynamicSearch<AgentToProjectMatching, Pessimistic.Solution>
{
	public static record Solution(AgentToProjectMatching matching, Pessimistic.Metric metric)
		implements nl.tudelft.aidm.optimalgroups.search.Solution<Pessimistic.Metric>
	{}

	public static class Metric implements Comparable<Metric>
	{
		private final AUPCR aupcr;
		private final WorstAssignedRank rank;

		public Metric(AgentToProjectMatching matching)
		{
			this.aupcr = new AUPCRStudent(matching);
			this.rank = new WorstAssignedRank.ProjectToStudents(matching);
		}

		public Metric(AUPCR aupcr, WorstAssignedRank worstAssignedRank)
		{
			this.aupcr = aupcr;
			this.rank = worstAssignedRank;
		}

		@Override
		public int compareTo(Pessimistic.Metric o)
		{
			// Check which solution has minimized the worst rank better
			var rankComparison = rank.compareTo(o.rank);

			// If the worst ranks are tied, look at AUPCR as tie breaker
			if (rankComparison != 0) return rankComparison;
			else return aupcr.compareTo(o.aupcr);
		}

	}


	// determine set of 'eccentric' students E - eccentric: student with lowest satisfaction
	// foreach s in E
	//     try all group combinations such that nobody in that group is worse off than s
	//     decrease slots of project p by 1


	public static void thingy(String[] args)
	{
		int k = 8;

		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		int minGroupSize = ce.groupSizeConstraint().minSize();

		var result = ce.allAgents().asCollection().stream()
			.map(agent -> agent.projectPreference().asListOfProjects())
			.map(projectPreference -> topNElements(projectPreference, k))
			.flatMap(Collection::stream)
			.collect(Collectors.groupingBy(project -> project)).entrySet().stream()
			.map(entry -> Pair.create(entry.getKey(), entry.getValue().size() / minGroupSize))
			.filter(pair -> pair.getValue() > 0)
			.sorted(Comparator.comparing((Pair<Project, Integer> pair) -> pair.getValue()))
	//			.mapToInt(pair -> pair.getValue())
	//			.sum();
//			.count();
				.collect(Collectors.toList());

//		ce = new CourseEditionModNoPeerPref(ce);
		var bepSysMatchingWhenNoPeerPrefs = new GroupProjectAlgorithm.BepSys().determineMatching(ce);

		var metrics = new MatchingMetrics.StudentProject(AgentToProjectMatching.from(bepSysMatchingWhenNoPeerPrefs));

		return;
	}

	public static void main(String[] args)
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		var thing = new Pessimistic(ce.allAgents(), ce.allProjects(), ce.groupSizeConstraint());
		thing.determineK();
	}

	public static <T> List<T> topNElements(List<T> list, int n)
	{
		return list.subList(0, n);
	}

	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;

	public Pessimistic(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		super(new Solution(new AgentToProjectMatching()
			{
				@Override
				public List<Match<Agent, Project>> asList()
				{
					return List.of();
				}

				@Override
				public DatasetContext datasetContext()
				{
					return agents.datsetContext;
				}
			}, new Metric(new AUPCR()
			{
				@Override
				public void printResult()
				{
				}

				@Override
				protected float totalArea()
				{
					return 1;
				}

				@Override
				protected int aupc()
				{
					return 0;
				}
			}, new WorstAssignedRank()
			{
				@Override
				public Integer asInt()
				{
					return projects.count();
				}
			}))
		);
		this.agents = agents;
		this.projects = projects;
		this.groupSizeConstraint = groupSizeConstraint;
	}

	private static record KAgents(List<Agent> agents, int k){}
	private static record Edge(Agent from, Project to, int rank){}

	public KAgents determineK()
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

		List<Agent>[] agentsWithK = new List[projects.count()];
		for (int i = 0; i < agentsWithK.length; i++)
		{
			agentsWithK[i] = new ArrayList<>();
		}

//		int[] cum260 = new int[43];
//		var proj260 = projects.findWithId(260).orElseThrow();
//		agents.forEach(agent -> {
//			agent.projectPreference().rankOf(proj260)
//				.ifPresent(rank -> cum260[rank] += 1);
//		});

		int k = 1;
		for (var thisAgent : agents.asCollection()) {
			int l = projects.count();

			var prefs = thisAgent.projectPreference().asListOfProjects();
			for (int i = prefs.size(); i >= 1; i--) {
				final var rankThisAgent = i;
				var proj = prefs.get(rankThisAgent-1);
				int count = (int) edgesTo.get(proj).stream()
					.filter(edge -> edge.from != thisAgent)
					.filter(edge -> edge.rank <= rankThisAgent)
					.count();
				if (count >= groupSizeConstraint.minSize() && rankThisAgent < l) {
					l = rankThisAgent;
				}
			}

			agentsWithK[l].add(thisAgent);

			k = Math.max(l, k);
		}

		return new KAgents(agentsWithK[k], k);
	}
}
