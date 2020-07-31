package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.metric.rank.WorstAssignedRank;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.search.DynamicSearch;
import org.apache.commons.math3.util.Pair;
import plouchtch.lang.exception.ImplementMe;

import java.util.*;
import java.util.stream.Collectors;

public class Pessimistic extends DynamicSearch<AgentToProjectMatching, Pessimistic.Solution>
{


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
//		thing.determineK();

		var matching = thing.matching();

		var metrics = new MatchingMetrics.StudentProject(matching);

		return;
	}

	public static <T> List<T> topNElements(List<T> list, int n)
	{
		return list.subList(0, n);
	}

	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;
	private final PossibleGroups possibleGroups;

	public Pessimistic(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		super(new Solution(new EmptyMatching(agents.datsetContext), new EmptyMetric()));

		this.agents = agents;
		this.projects = projects;
		this.groupSizeConstraint = groupSizeConstraint;
		this.possibleGroups = new PossibleGroups();
	}

	public AgentToProjectMatching matching()
	{
		var root = new PessimismSearchNode(agents, new DecrementableProjects(projects), groupSizeConstraint);
		var solution = root.solution();

		return solution.orElseThrow()
			.matching;
	}

	public class PessimismSearchNode extends SearchNode
	{
		private final Agents agents;
		private final DecrementableProjects projects;
		private final GroupSizeConstraint groupSizeConstraint;

		PessimismSearchNode(Agents agents, DecrementableProjects projects, GroupSizeConstraint groupSizeConstraint)
		{
			this.agents = agents;
			this.projects = projects;
			this.groupSizeConstraint = groupSizeConstraint;
		}

		@Override
		public Optional<Solution> solve()
		{
			if (agents.count() < groupSizeConstraint.minSize()) {
				// TODO Be smarter: we can check if all agents can be grouped without remainders sooner
				return Optional.empty();
			}

			var kProjects = KProjectAgentsPairing.from(agents, projects, groupSizeConstraint);

			var solution = kProjects.pairingsAtK().stream()
				.flatMap(pairing -> {
					var possibleGroupmates = new LinkedHashSet<>(pairing.possibleGroupmates());
					var possibleGrps =  possibleGroups.of(pairing.agents(), possibleGroupmates, groupSizeConstraint);
					var nodes = possibleGrps
						.stream()
						.flatMap(possibleGroup -> {
							Agents agentsWithoutGroup = agents.without(possibleGroup);
							DecrementableProjects projectsWithout = this.projects.decremented(pairing.project());
							var solutionsStream = new PessimismSearchNode(agentsWithoutGroup, projectsWithout, groupSizeConstraint).solution().stream();
							return solutionsStream;
						});

					return nodes;
				})
				.max(Comparator.comparing(Solution::metric));

			return solution;
		}

		@Override
		protected boolean candidateSolutionTest(AgentToProjectMatching candidateSolution)
		{
			// I think...
			return true;
		}


	}

	public static class DecrementableProjects extends ListBasedProjects
	{
		private final Map<Project, Integer> projectSlotUtilization;

		private final Projects projects;
		private List<Project> projectsAvailable;

		public DecrementableProjects(Projects projects)
		{
			this(projects, emptyUtilization(projects));
		}

		public DecrementableProjects decremented(Project project)
		{
			IdentityHashMap<Project, Integer> updatedUtilization = new IdentityHashMap<>(projectSlotUtilization);
			updatedUtilization.merge(project, 1, (currUtil, decrement) -> currUtil - decrement);

			return new DecrementableProjects(projects, updatedUtilization);
		}

		@Override
		protected List<Project> projectList()
		{
			if (projectsAvailable == null)
				this.projectsAvailable = projects.asCollection().stream()
				.filter(project -> projectSlotUtilization.get(project) < project.slots().size())
				.collect(Collectors.toList());

			return projectsAvailable;
		}

		private DecrementableProjects(Projects projects, Map<Project, Integer> projectSlotUtilization)
		{
			this.projects = projects;
			this.projectSlotUtilization = projectSlotUtilization;
		}

		private static Map<Project, Integer> emptyUtilization(Projects projects)
		{
			Map<Project, Integer> utilization = new IdentityHashMap<>(projects.count());
			projects.forEach(project -> utilization.put(project, 0));

			return utilization;
		}
	}

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
			var henk  = 0;
		}

		public Metric(AUPCR aupcr, WorstAssignedRank worstAssignedRank)
		{
			this.aupcr = aupcr;
			this.rank = worstAssignedRank;
			var henk  = 0;
		}

		@Override
		public String toString()
		{
			return "Metric - worst: " + rank.asInt() + ", aupcr: " + aupcr.asDouble();
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

	private static class EmptyMatching implements AgentToProjectMatching
	{
		private final DatasetContext datasetContext;

		public EmptyMatching(DatasetContext datasetContext)
		{
			this.datasetContext = datasetContext;
		}

		@Override
		public List<Match<Agent, Project>> asList()
		{
			return List.of();
		}

		@Override
		public DatasetContext datasetContext()
		{
			return datasetContext;
		}
	}

	private static class EmptyMetric extends Metric
	{
		private EmptyMetric()
		{
			super(new ZeroAupcr(), new HugeWorstRank());
		}

		private static class ZeroAupcr extends AUPCR
		{
			@Override
			public void printResult()
			{
				throw new ImplementMe();
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
		}

		private static class HugeWorstRank implements WorstAssignedRank
		{
			@Override
			public Integer asInt()
			{
				return Integer.MAX_VALUE;
			}
		}
	}
}
