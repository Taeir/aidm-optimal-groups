package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.group.GroupFactorization;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.group.PossibleGroupings;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.group.PossibleGroupingsByIndividual;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model.DecrementableProjects;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model.PessimismSolution;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.ProjectPairings;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.search.DynamicSearch;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WorstHumblePairingsSearchBFS extends DynamicSearch<AgentToProjectMatching, PessimismSolution>
{

	public static void main(String[] args)
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		var thing = new WorstHumblePairingsSearchBFS(ce.allAgents(), ce.allProjects(), ce.groupSizeConstraint());
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

	public WorstHumblePairingsSearchBFS(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		super(PessimismSolution.empty(agents.datasetContext));

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
				System.out.println("Pessimism BFS: going in over-time...");
				forkJoinPool.awaitTermination(10, TimeUnit.MINUTES);
			}
			forkJoinPool.shutdownNow();
		}
		catch (Exception e) {
			Thread.currentThread().interrupt();
		}

		Assert.that(bestSolutionSoFar.hasNonEmptySolution())
			.orThrowMessage("Pessimism-search did not find a single valid solution :");

		var matching = bestSolutionSoFar.currentBest().matching();

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
		var emptySolution = PessimismSolution.empty(datsetContext);


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
//				nodesToExpand.add(nodes.poll());s
//			}

			var newNodes = nodes.parallelStream()
				.takeWhile(searchNode -> counter.get() <= 4 && searchNode.k <= bestSolutionSoFar.currentBest().metric().worstRank().asInt())
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
		while (counter.getPlain() != 0 && !nodes.isEmpty() && nodes.peek().k <= bestSolutionSoFar.currentBest().metric().worstRank().asInt());

//			nodes.stream().takeWhile(node -> node.partial.metric().)

//			if (nodes.size() > 0 && nodes.peek() == bestSolutionSoFar.currentBest())
//				return Optional.of(bestSolutionSoFar.currentBest());
	}


	public class PessimismSearchNode implements Comparable<PessimismSearchNode>
	{
		private final PessimismSolution partial;

		private final Agents agents;
		private final DecrementableProjects projects;
		private final GroupSizeConstraint groupSizeConstraint;
		private Optional<ProjectPairings> kProjectAgentsPairing;
		private final Integer k;

		PessimismSearchNode(PessimismSolution partial, Agents agents, DecrementableProjects projects, GroupSizeConstraint groupSizeConstraint)
		{
			this.partial = partial;
			this.agents = agents;
			this.projects = projects;
			this.groupSizeConstraint = groupSizeConstraint;
			this.k = partial.metric().worstRank().asInt();
		}

		PessimismSearchNode(PessimismSearchNode searchNode, ProjectPairings projectPairings)
		{
			this.partial = searchNode.partial;
			this.agents = searchNode.agents;
			this.projects = searchNode.projects;
			this.groupSizeConstraint = searchNode.groupSizeConstraint;
			this.k = Math.max(searchNode.k, projectPairings.k());

			this.kProjectAgentsPairing = Optional.of(projectPairings);
		}

		private Optional<ProjectPairings> determineKProjectAgentsPairing()
		{
			if (kProjectAgentsPairing == null) {
				var bestWorstRankSoFar = bestSolutionSoFar.currentBest().metric().worstRank().asInt();
				return ProjectPairings.from(agents, projects, groupSizeConstraint, bestWorstRankSoFar);
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

			var kProjectsMaybe = determineKProjectAgentsPairing();
			if (kProjectsMaybe.isEmpty()) {
				return List.of();
			}

			var kProjects = kProjectsMaybe.get();

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

//				newlyExpandedNodes.ensureCapacity(newlyExpandedNodes.size() + possibleGrps.size());

				var iter = possibleGrps.iterator();
				while (iter.hasNext())
				{
					var possibleGroup = iter.next();

					int agentsRemainingAfterMakingGroup = agents.count() - possibleGroup.size();
					if (!groupFactorization.isFactorableIntoValidGroups(agentsRemainingAfterMakingGroup)) {
						// If forming this group leads to an invalid solution (cannot partition remaining students into
						// groups of valid sizes), then try some other group
						continue;
					}

					Agents remainingAgents = agents.without(possibleGroup);
					DecrementableProjects projectsWithout = this.projects.decremented(pairing.project());

					var newPartial = partial.matching().withMatches(pairing.project(), possibleGroup);
					var newPartialAsSolution = PessimismSolution.fromMatching(newPartial);

					if (remainingAgents.count() == 0) {
						bestSolutionSoFar.potentiallyUpdateBestSolution(newPartialAsSolution);
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
		public int compareTo(PessimismSearchNode o)
		{
			return this.k.compareTo(o.k);
		}
	}

}
