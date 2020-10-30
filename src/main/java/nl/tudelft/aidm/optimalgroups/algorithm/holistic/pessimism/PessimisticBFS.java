package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism.groups.PossibleGroupings;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism.groups.PossibleGroupingsByIndividual;
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
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.search.DynamicSearch;
import org.jetbrains.annotations.NotNull;
import plouchtch.assertion.Assert;
import plouchtch.lang.exception.ImplementMe;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PessimisticBFS extends DynamicSearch<AgentToProjectMatching, PessimisticBFS.Solution>
{

	public static void main(String[] args)
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		var thing = new PessimisticBFS(ce.allAgents(), ce.allProjects(), ce.groupSizeConstraint());
//		thing.determineK();

		var indiff = ce.allAgents().asCollection().stream()
			.filter(agent -> agent.projectPreference().isCompletelyIndifferent())
			.collect(Collectors.toList());

		var matching = thing.matching();

		var metrics = new MatchingMetrics.StudentProject(matching);

		return;
	}

	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;
	private final PossibleGroupings possibleGroups;
	private final GroupFactorization groupFactorization;

	public PessimisticBFS(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		super(new Solution(new EmptyMatching(agents.datasetContext), new EmptyMetric()));

		this.agents = agents;
		this.projects = projects;
		this.groupSizeConstraint = groupSizeConstraint;
		this.possibleGroups = new PossibleGroupingsByIndividual();

		this.groupFactorization = new GroupFactorization(groupSizeConstraint, agents.count());
	}

	public AgentToProjectMatching matching()
	{

		// Run the algorithm with time constraints, after timeout we check best solution found up to that moment
		try {
			final ForkJoinPool forkJoinPool = new ForkJoinPool();

			// Threads of a parallel stream run in a pool, not as children of this thread
			// Hence, we provide a pool context which we control so that we can force shutdown
			forkJoinPool.execute(this::mainLoop);
			forkJoinPool.awaitTermination(5, TimeUnit.MINUTES);
			if (bestSolutionSoFar.hasNonEmptySolution() == false) {
				// Give an extension...
				forkJoinPool.awaitTermination(10, TimeUnit.MINUTES);
			}
			forkJoinPool.shutdownNow();
		}
		catch (Exception e) {
			Thread.currentThread().interrupt();
		}

		Assert.that(bestSolutionSoFar.hasNonEmptySolution())
			.orThrowMessage("Pessimism-search did not find a single valid solution :");

		var matching = bestSolutionSoFar.currentBest().matching;

		// Check all students matched
		Assert.that(agents.count() == matching.countDistinctStudents())
			.orThrowMessage("Not all agents were matched");

		// Check if all size constraints met as well
		var matchingGroupedByProject = matching.groupedByProject();
		for (var projectWithMatches : matchingGroupedByProject.entrySet()) {
			var project = projectWithMatches.getKey();
			var matches = projectWithMatches.getValue();
			Assert.that(groupFactorization.isFactorableIntoValidGroups(matches.size()))
				.orThrowMessage("Students matched to a project cannot be partitioned into groups");
		}

		return matching;
	}

	public void mainLoop()
	{
		DatasetContext datsetContext = agents.datasetContext;
		var emptySolution = new Solution(new EmptyMatching(datsetContext), new EmptyMetric());


		PriorityQueue<PessimismSearchNode> nodes = new PriorityQueue<>();

		var root = new PessimismSearchNode(emptySolution, agents, new DecrementableProjects(projects), groupSizeConstraint);
		nodes.add(root);

		var threadGroup = Thread.currentThread().getThreadGroup();
		threadGroup.setMaxPriority(0);

		var counter = new AtomicInteger(0);

		do {
			counter.setPlain(0);

//			var nodesToExpand = new ArrayList<PessimismSearchNode>();
//			while (!nodes.isEmpty() && nodes.peek().k <= bestSolutionSoFar.currentBest().metric().absoluteWorstRank.asInt()) {
//				nodesToExpand.add(nodes.poll());
//			}

			var newNodes = nodes.parallelStream()
				.takeWhile(searchNode -> counter.get() <= 4 && searchNode.k <= bestSolutionSoFar.currentBest().metric().absoluteWorstRank.asInt())
				.peek((__) -> counter.getAndIncrement())
				.flatMap(searchNode -> searchNode.expand().stream())
				.collect(Collectors.toList());

			// remove those that are expanded, these are the first $counter.get()$ nodes in the nodes queue
			for (int i = 0; i < counter.getPlain(); i++)
			{
				nodes.poll();
			}

			nodes.addAll(newNodes);

		}
		while (counter.getPlain() != 0 && !nodes.isEmpty() && nodes.peek().k <= bestSolutionSoFar.currentBest().metric().absoluteWorstRank.asInt());

//			nodes.stream().takeWhile(node -> node.partial.metric().)

//			if (nodes.size() > 0 && nodes.peek() == bestSolutionSoFar.currentBest())
//				return Optional.of(bestSolutionSoFar.currentBest());
	}


	public class PessimismSearchNode implements Comparable<PessimismSearchNode>
	{
		private final Solution partial;

		private final Agents agents;
		private final DecrementableProjects projects;
		private final GroupSizeConstraint groupSizeConstraint;
		private KProjectAgentsPairing kProjectAgentsPairing;
		private final Integer k;

		PessimismSearchNode(Solution partial, Agents agents, DecrementableProjects projects, GroupSizeConstraint groupSizeConstraint)
		{
			this.partial = partial;
			this.agents = agents;
			this.projects = projects;
			this.groupSizeConstraint = groupSizeConstraint;
			this.k = partial.metric.absoluteWorstRank.asInt();
		}

		PessimismSearchNode(PessimismSearchNode searchNode, KProjectAgentsPairing kProjectAgentsPairing)
		{
			this.partial = searchNode.partial;
			this.agents = searchNode.agents;
			this.projects = searchNode.projects;
			this.groupSizeConstraint = searchNode.groupSizeConstraint;
			this.k = Math.max(searchNode.k, kProjectAgentsPairing.k());

			this.kProjectAgentsPairing = kProjectAgentsPairing;
		}

		private KProjectAgentsPairing determineKProjectAgentsPairing()
		{
			if (kProjectAgentsPairing == null) {
				return KProjectAgentsPairing.from(agents, projects, groupSizeConstraint);
			}

			return kProjectAgentsPairing;
		}

		public Collection<PessimismSearchNode> expand()
		{
			// BOUND
			// Check if the partial solution has a worse absolute worst rank than the best-so-far
			// If it is indeed worse, do not continue searching further with this partial solution (it will never become beter)
//			if (bestSolutionSoFar.test(best -> best.metric.absoluteWorstRank.asInt() < partial.metric.absoluteWorstRank.asInt())) {
//				return Optional.empty();
//			}

			// 1. If this node is asked to expand, then it's partial solution is tied for best-so-far
			// 2. However, after determining the next worst-best pairing we do need to compare that k with best-so far
			//     - but, what if it's bad?
			//  -> a] We update this node... with new worst without completing the expansion/adding to partial?
			//     b] We expand the node regardless

			var kProjects = determineKProjectAgentsPairing();

			// If the first step of expanding this node already gives us a worse max-rank than that of
			// the partial solution so-far in the tree, we update the k (max-rank so far) of the node and
			// return the updated one. It will be expanded if there are no nodes with better k's left to expand
			if (kProjects.k() > this.k) {
				return List.of(
					new PessimismSearchNode(partial, agents, projects, groupSizeConstraint)
				);
			}


			var newlyExpandedNodes = new ArrayList<PessimismSearchNode>(0);

			for (var pairing : kProjects.pairingsAtK())
			{
				var possibleGroupmates = new LinkedHashSet<>(pairing.possibleGroupmates());
				var possibleGrps =  possibleGroups.of(pairing.agents(), possibleGroupmates, groupSizeConstraint);

				newlyExpandedNodes.ensureCapacity(newlyExpandedNodes.size() + possibleGrps.size());

				for (var possibleGroup : possibleGrps)
				{
					int agentsRemainingAfterMakingGroup = agents.count() - possibleGroup.size();
					if (!groupFactorization.isFactorableIntoValidGroups(agentsRemainingAfterMakingGroup)) {
						// If forming this group leads to an invalid solution (cannot partition remaining students into
						// groups of valid sizes), then try some other group
						continue;
					}

					Agents remainingAgents = agents.without(possibleGroup);
					DecrementableProjects projectsWithout = this.projects.decremented(pairing.project());

					var newPartial = partial.matching().withMatches(pairing.project(), possibleGroup);
					Solution newPartialAsSolution = Solution.fromMatching(newPartial);

					if (remainingAgents.count() == 0) {
						bestSolutionSoFar.potentiallyUpdateBestSolution((bestSoFar) -> {
							if (bestSoFar.metric().compareTo(newPartialAsSolution.metric()) < 0) {
								return Optional.of(newPartialAsSolution);
							}

							return Optional.empty();
						});
					}
					else {
						var node = new PessimismSearchNode(newPartialAsSolution, remainingAgents, projectsWithout, groupSizeConstraint);
						newlyExpandedNodes.add(node);
					}
				}
			}

//			nodes.stream().takeWhile(node -> node.partial.metric().)

//			if (nodes.size() > 0 && nodes.peek() == bestSolutionSoFar.currentBest())
//				return Optional.of(bestSolutionSoFar.currentBest());

			return newlyExpandedNodes;
		}

		@Override
		public int compareTo(@NotNull PessimisticBFS.PessimismSearchNode o)
		{
			return this.k.compareTo(o.k);
		}
	}

	private static record PairingWithPossibleGroup(ProjectAgentsPairing pairing, Set<Agent> possibleGroup) {}

	public static record Solution(PessimismMatching matching, PessimisticBFS.Metric metric)
		implements nl.tudelft.aidm.optimalgroups.search.Solution<PessimisticBFS.Metric>
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
		public int compareTo(PessimisticBFS.Metric o)
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
			var matchedWithNew = new ArrayList<Match<Agent, Project>>(this.asList().size() + agents.size());
			matchedWithNew.addAll(this.asList());

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
