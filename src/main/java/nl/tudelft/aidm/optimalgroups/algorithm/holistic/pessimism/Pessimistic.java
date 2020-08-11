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
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.ListBasedMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.search.DynamicSearch;
import org.apache.commons.math3.util.Pair;
import plouchtch.lang.exception.ImplementMe;

import java.time.Duration;
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
	private final GroupFactorization groupFactorization;

	public Pessimistic(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		super(new Solution(new EmptyMatching(agents.datsetContext), new EmptyMetric()));

		this.agents = agents;
		this.projects = projects;
		this.groupSizeConstraint = groupSizeConstraint;
		this.possibleGroups = new PossibleGroups();

		this.groupFactorization = new GroupFactorization(groupSizeConstraint, agents.count());
	}

	public AgentToProjectMatching matching()
	{
		DatasetContext datsetContext = agents.datsetContext;
		var emptySolution = new Solution(new EmptyMatching(datsetContext), new EmptyMetric());

		var root = new PessimismSearchNode(emptySolution, agents, new DecrementableProjects(projects), groupSizeConstraint);

		var thread = new Thread(root::solution);
		thread.start();

		try {
			thread.join(Duration.ofMinutes(5).toMillis());
			thread.interrupt();
		}
		catch (Exception e) {
			Thread.currentThread().interrupt();
		}

		List<Solution> hacky = new ArrayList<>(1);
		bestSolutionSoFar.test(bestSoFar -> {
			hacky.add(bestSoFar);
			return true;
		});

		return hacky.get(0).matching;
	}

	public class PessimismSearchNode extends SearchNode
	{
		private final Solution partial;

		private final Agents agents;
		private final DecrementableProjects projects;
		private final GroupSizeConstraint groupSizeConstraint;

		PessimismSearchNode(Solution partial, Agents agents, DecrementableProjects projects, GroupSizeConstraint groupSizeConstraint)
		{
			this.partial = partial;
			this.agents = agents;
			this.projects = projects;
			this.groupSizeConstraint = groupSizeConstraint;
		}

		@Override
		public Optional<Solution> solve()
		{
			// BOUND
			// Check if the partial solution has a worse absolute worst rank than the best-so-far
			// If it is indeed worse, do not continue searching further with this partial solution (it will never become beter)
			if (bestSolutionSoFar.test(best -> best.metric.absoluteWorstRank.asInt() < partial.metric.absoluteWorstRank.asInt())) {
				return Optional.empty();
			}

			// If node has no agents to group, the partial solution is considered to be done
			if (agents.count() == 0) {
				bestSolutionSoFar.potentiallyUpdateBestSolution(bestSoFar -> {
					if (bestSoFar.metric().compareTo(partial.metric()) < 0) {
						return Optional.of(partial);
					}

					return Optional.empty();
				});

				return Optional.of(partial);
			}

			// If the remaining agents cannot be partitioned into groups of valid sizes,
			// then this search branch is to be terminated without a solution (aka empty solution)
			if (!groupFactorization.isFactorableIntoValidGroups(agents.count())) {
				return Optional.empty();
			}

			var kProjects = KProjectAgentsPairing.from(agents, projects, groupSizeConstraint);

			var solution = kProjects.pairingsAtK()
//				.stream()
				.parallelStream()

				.flatMap(pairing -> {
					var possibleGroupmates = new LinkedHashSet<>(pairing.possibleGroupmates());
					var possibleGrps =  possibleGroups.of(pairing.agents(), possibleGroupmates, groupSizeConstraint);
					return possibleGrps.stream()
						.map(possibleGroup -> new PairingWithPossibleGroup(pairing, possibleGroup));
				})

				.map(pairingWithPossibleGroup -> {
					var possibleGroup = pairingWithPossibleGroup.possibleGroup();
					var pairing = pairingWithPossibleGroup.pairing();

					Agents agentsWithoutGroup = agents.without(possibleGroup);
					DecrementableProjects projectsWithout = this.projects.decremented(pairing.project());

					var newPartial = partial.matching().withMatches(pairing.project(), possibleGroup);
					return new PessimismSearchNode(Solution.fromMatching(newPartial), agentsWithoutGroup, projectsWithout, groupSizeConstraint);
				})

				.map(SearchNode::solution)

				// Unpack optionals - filter out empty/invalid solutions
				.flatMap(Optional::stream)

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

	private static record PairingWithPossibleGroup(ProjectAgentsPairing pairing, Set<Agent> possibleGroup) {}

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

	public static record Solution(PessimismMatching matching, Pessimistic.Metric metric)
		implements nl.tudelft.aidm.optimalgroups.search.Solution<Pessimistic.Metric>
	{
		public static Solution fromMatching(PessimismMatching matching) {
			return new Solution(matching, new Metric(matching));
		}
	}

	public static class Metric implements Comparable<Metric>
	{
		private final AUPCR aupcr;
		private final WorstAssignedRank absoluteWorstRank;

		public Metric(AgentToProjectMatching matching)
		{
			this.aupcr = new AUPCRStudent(matching);
			this.absoluteWorstRank = new WorstAssignedRank.ProjectToStudents(matching);
			var henk  = 0;
		}

		public Metric(AUPCR aupcr, WorstAssignedRank worstAssignedRank)
		{
			this.aupcr = aupcr;
			this.absoluteWorstRank = worstAssignedRank;
			var henk  = 0;
		}

		@Override
		public String toString()
		{
			return "Metric - worst: " + absoluteWorstRank.asInt() + ", aupcr: " + aupcr.asDouble();
		}

		@Override
		public int compareTo(Pessimistic.Metric o)
		{
			// Check which solution has minimized the worst rank better
			// Smaller rank is better, we also want to "maximize" the metric
			// and AUPCR is also "higher is better". So inverse the compareTo
			var rankComparison = -(absoluteWorstRank.compareTo(o.absoluteWorstRank));

			// If the worst-ranks are tied, use AUPCR as tie breaker
			if (rankComparison == 0) return aupcr.compareTo(o.aupcr);
			else return rankComparison;
		}
	}

	private static class PessimismMatching extends ListBasedMatching<Agent, Project> implements AgentToProjectMatching
	{
		public PessimismMatching(DatasetContext datasetContext, List<Match<Agent, Project>> matches)
		{
			super(datasetContext, matches);
		}

		public PessimismMatching(List<Match<Agent, Project>> matches)
		{
			super(
				matches.stream().map(match -> match.from().context).findAny().orElseThrow(),
				List.copyOf(matches)
			);
		}

		public PessimismMatching withMatches(Project project, Collection<Agent> agents)
		{
			var matchedWithNew = new ArrayList<>(asList());
			agents.forEach(agent -> {
				var match = new AgentToProjectMatch(agent, project);
				matchedWithNew.add(match);
			});

			return new PessimismMatching(datasetContext(), matchedWithNew);
		}
	}

	private static class EmptyMatching extends PessimismMatching
	{
		public EmptyMatching(DatasetContext datasetContext)
		{
			super(datasetContext, List.of());
		}

		@Override
		public List<Match<Agent, Project>> asList()
		{
			return List.of();
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
