package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.group.GroupFactorization;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model.DecrementableProjects;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model.PessimismSolution;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.MinQuorumRequirement;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.NumAgentsTillQuorum;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.WorstAmongBestProjectPairings;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model.MatchCandidate;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.spdc.ActiveProjects;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.math.CombinationsOfObjects;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.search.DynamicSearch;
import org.jetbrains.annotations.NotNull;
import plouchtch.assertion.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A variant of the "Pessimism" search where the BnB search is _not_ creating groups, but merely
 * matches the most disadvantaged (at that step) students to a project. Doing so evolves the set of "active projects"
 * as introduced by Tumennasan et al for in the SDPC mechanism {@link ActiveProjects}. But now we're doing a Branch n Bound
 * search instead of a serial dictatorship, the order is dynamic - driven by the heuristic, on the choices of which we branch
 */
@SuppressWarnings("DuplicatedCode")
public class HumbleMiniMaxWithClosuresSearch extends DynamicSearch<AgentToProjectMatching, PessimismSolution>
{
	public static void main(String[] args)
	{
//		var ce = DatasetContextTiesBrokenIndividually.from(CourseEdition.fromLocalBepSysDbSnapshot(10));
		var ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		var thing = new HumbleMiniMaxWithClosuresSearch(ce.allAgents(), ce.allProjects(), ce.groupSizeConstraint());
//		thing.determineK();

		var matching = thing.matching();

		var metrics = new MatchingMetrics.StudentProject(matching);

		return;
	}

	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;
	private final PossibleAssignmentOfAgentsToProject possibleAssignmentOfAgentsToProject;
	private final GroupFactorization groupFactorization;

	public HumbleMiniMaxWithClosuresSearch(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		this(agents, projects, groupSizeConstraint, projects.count() + 1);
	}

	public HumbleMiniMaxWithClosuresSearch(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint, int worstRankBound)
	{
		super(PessimismSolution.emptyWithBoundedWorstRank(agents.datasetContext, worstRankBound));

		this.agents = agents;
		this.projects = projects;
		this.groupSizeConstraint = groupSizeConstraint;
		this.possibleAssignmentOfAgentsToProject = new PossibleAssignmentOfAgentsToProject();

		this.groupFactorization = new GroupFactorization(groupSizeConstraint, agents.count());
	}

	public AgentToProjectMatching matching()
	{
		DatasetContext datsetContext = agents.datasetContext;

		var root = new HumbleMiniMaxClosuresSearchNode(agents, new DecrementableProjects(projects), groupSizeConstraint);

		// Run the algorithm with time constraints, after timeout we check best solution found up to that moment
		try {
			final ForkJoinPool forkJoinPool = new ForkJoinPool();

			// Threads of a parallel stream run in a pool, not as children of this thread
			// Hence, we provide a pool context which we control so that we can force shutdown
			forkJoinPool.execute(root::solution);
			forkJoinPool.awaitTermination(5, TimeUnit.MINUTES);
			if (bestSolutionSoFar.hasNonEmptySolution() == false) {
				// Give an extension...
				System.out.println("Pessimism: entering over-time...");
				forkJoinPool.awaitTermination(1, TimeUnit.MINUTES);
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

	private static record AgentsToProjectPairing(List<Agent> agents, Project project, int rankOfProject) {}

	public class HumbleMiniMaxClosuresSearchNode extends SearchNode
	{
		private final PessimismSolution partial;

		private final Agents remainingAgents;
		private final ActiveProjects activeProjects;
		private final GroupSizeConstraint groupSizeConstraint;

		public HumbleMiniMaxClosuresSearchNode(Agents allAgents, Projects allProjects, GroupSizeConstraint groupSizeConstraint)
		{
			this.partial = PessimismSolution.empty(allAgents.datasetContext);
			this.remainingAgents = allAgents;
			this.activeProjects = new ActiveProjects(partial.matching(), allProjects, remainingAgents, groupSizeConstraint);
			this.groupSizeConstraint = groupSizeConstraint;
		}

		private HumbleMiniMaxClosuresSearchNode(PessimismSolution partial, Agents remainingAgents, ActiveProjects activeProjects, GroupSizeConstraint groupSizeConstraint)
		{
			this.partial = partial;
			this.remainingAgents = remainingAgents;
			this.activeProjects = activeProjects;
			this.groupSizeConstraint = groupSizeConstraint;
		}

		@Override
		public Optional<PessimismSolution> solve()
		{
			if (remainingAgents.count() == 0) {
				bestSolutionSoFar.potentiallyUpdateBestSolution(partial);
				return Optional.of(partial);
			}

			// BOUND
			// Check if the partial solution has a worse absolute worst rank than the best-so-far
			// If it is indeed worse, do not continue searching further with this partial solution (it will never become beter)
			boolean partialIsAlreadyWorseThanCurrentBest = partial.matching().asList().size() != 0 && bestSolutionSoFar.test(best -> best.isBetterThan(partial));
			if (partialIsAlreadyWorseThanCurrentBest) {
				return Optional.empty();
			}

			// By using ActiveProjects not relevant
//			if (!groupFactorization.isFactorableIntoValidGroups(agents.count())) {
//				return Optional.empty();
//			}

			var bestWorstRankSoFar = bestSolutionSoFar.currentBest().metric().worstRank().asInt();

//			MinQuorumRequirement minQuorumRequirement = new MinQuorumReqTillNextQuorum();

			// TODO: tigher check - extend ActiveProjects to count available slots for students
			if (activeProjects.count() < 1) {
				return Optional.empty();
			}

			var essentialPairing = WorstAmongBestProjectPairings.from(remainingAgents, activeProjects, project -> new NumAgentsTillQuorum(1), bestWorstRankSoFar);

//			if (essentialPairing.isEmpty()) {
//				return Optional.empty();
//			}

			var solution = essentialPairing.stream().parallel()
				.flatMap(p -> p.pairingsAtK().stream())

				.flatMap(this::intoAllPossibleGroupCombinationsPerPairing)

				// Abort the parallel-stream if the bound has changed in the meantime, as it will not find better solutions
				.takeWhile(x -> x.rankOfProject() <= bestSolutionSoFar.currentBest().metric().worstRank().asInt())

				.map(this::assumeGroupAndRecurseDeeper)

				.map(SearchNode::solution)

				// Unpack optionals - filter out empty/invalid solutions
				.flatMap(Optional::stream)

				.max(Comparator.comparing(PessimismSolution::metric));

			return solution;
		}

		@NotNull
		private Stream<? extends AgentsToProjectPairing> intoAllPossibleGroupCombinationsPerPairing(MatchCandidate pairing)
		{
			// TODO OPT: group agents by "type" to decrease the amount of symmetric combinations. Where type is to be defined as
			//  "only the effective part of the agent's preference".

			var numAlreadyMatched = partial.matching().groupedByProject().getOrDefault(pairing.project(), List.of()).size();

			// Wrong!
			int maxCapacityOfProject = pairing.project().slots().size() * groupSizeConstraint.maxSize();
			var remainingCapacityProject = maxCapacityOfProject - numAlreadyMatched;

			var possibleGrps = possibleAssignmentOfAgentsToProject.of(pairing.agents(), remainingCapacityProject);

			return possibleGrps.map(candidates -> new AgentsToProjectPairing(candidates, pairing.project(), pairing.kRank()));
		}

		private HumbleMiniMaxClosuresSearchNode assumeGroupAndRecurseDeeper(AgentsToProjectPairing pairing)
		{
			var candidates = pairing.agents();
			var project = pairing.project();

			Agents remainingAgents = this.remainingAgents.without(candidates);
//			DecrementableProjects remainingProjects = this.projects.decremented(project);

			var newPartial = partial.matching().withMatches(project, candidates);
			var solutionSoFar = PessimismSolution.fromMatching(newPartial);

			var availableProjectsAfter = new ActiveProjects(solutionSoFar.matching(), activeProjects, remainingAgents, groupSizeConstraint);

			return new HumbleMiniMaxClosuresSearchNode(solutionSoFar, remainingAgents, availableProjectsAfter, groupSizeConstraint);
		}

		@Override
		protected boolean candidateSolutionTest(AgentToProjectMatching candidateSolution)
		{
			// I think...
			return true;
		}

		private class MinQuorumReqTillNextQuorum implements MinQuorumRequirement
		{
			@Override
			public NumAgentsTillQuorum forProject(Project project)
			{
				var partialGroupedByProject = partial.matching().groupedByProject();
				var currentlyMatchedToProject = partialGroupedByProject.get(project);

				int numCurrentlyMatchedToProject = currentlyMatchedToProject == null ? 0 : currentlyMatchedToProject.size();

				if (numCurrentlyMatchedToProject < groupSizeConstraint.minSize()) {
					return new NumAgentsTillQuorum(groupSizeConstraint.minSize() - numCurrentlyMatchedToProject);
				}

				if (numCurrentlyMatchedToProject >= groupSizeConstraint.minSize() && numCurrentlyMatchedToProject < groupSizeConstraint.maxSize()) {
					return new NumAgentsTillQuorum(groupSizeConstraint.maxSize() - numCurrentlyMatchedToProject);
				}

//				if (numCurrentlyMatchedToProject == groupSizeConstraint.maxSize()) {
//					return new NumAgentsTillQuorum(0);
//				}

				// We're over the max-group-size, so we have to find the next number that is factorable into groups
				// for example, let g-min=4 and g-max=6, then if numCurrentlyMatched is 6, the NumAgentsTillQuorum is 2
				// because 2x groups of 4 is the next number of students that can be divided into a valid number of validly
				// sized groups.
				var groupsFactorisation = GroupFactorization.cachedInstanceFor(groupSizeConstraint);
				var upperbound = project.slots().size() * groupSizeConstraint.maxSize();
				for (var i = numCurrentlyMatchedToProject; i <= upperbound; i++) {
					if (groupsFactorisation.isFactorableIntoValidGroups(i))
						return new NumAgentsTillQuorum(i - numCurrentlyMatchedToProject);
				}

				throw new RuntimeException("BUGCHECK: Something not working well");
			}
		}
	}

	private static class PossibleAssignmentOfAgentsToProject
	{
		public Stream<List<Agent>> of(Set<Agent> agents, int capacity)
		{
			int maxAgentsToAssign = Math.min(agents.size(), capacity);
			return IntStream.rangeClosed(1, maxAgentsToAssign).boxed()
				.flatMap(numToAssign -> new CombinationsOfObjects<>(agents, numToAssign).asStream());
		}
	}



}
