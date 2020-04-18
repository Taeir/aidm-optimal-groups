package nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp;

import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatchings;
import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMaxFlowMatchings;
import nl.tudelft.aidm.optimalgroups.metric.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.AssignedProjectRankGroup;
import nl.tudelft.aidm.optimalgroups.metric.GroupPreferenceSatisfaction;
import nl.tudelft.aidm.optimalgroups.model.*;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.match.FormedGroupToProjecMatchings;
import nl.tudelft.aidm.optimalgroups.model.match.GroupToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.sql2o.GenericDatasource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ILPPPDeterminedMatchings implements GroupProjectMatchings<Group.FormedGroup>
{
	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;

	private FormedGroupToProjecMatchings matchings = null;

	// for testing
	public static void main(String[] args)
	{
		var dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
		var matching = new ILPPPDeterminedMatchings(CourseEdition.fromBepSysDatabase(dataSource, 10));

		List<Match<Group.FormedGroup, Project>> matches = matching.asList();

		for (var match : matches) {
			AssignedProjectRankGroup assignedProjectRank = new AssignedProjectRankGroup(match);

			int rankNumber = assignedProjectRank.groupRank();
			System.out.println("Group " + match.from().groupId() + " got project " + match.to().id() + " (ranked as number " + rankNumber + ")");

			assignedProjectRank.studentRanks().forEach(metric -> {
				System.out.printf("\t\t-Student %s", metric.student().id);
				System.out.printf(", rank: %s", metric.studentsRank());

				System.out.printf("\t\t group satisfaction: %s\n", new GroupPreferenceSatisfaction(match, metric.student()).asFraction());
			});
		}

		return;
	}

	public ILPPPDeterminedMatchings(CourseEdition courseEdition)
	{
		this.agents = courseEdition.agents;
		this.projects = courseEdition.projects;
		this.groupSizeConstraint = courseEdition.groupSizeConstraint;
	}

	@Override
	public List<Match<Group.FormedGroup, Project>> asList()
	{
		if (matchings == null) {
			matchings = determineMatching();
		}

		return matchings.asList();
	}

	public FormedGroupToProjecMatchings determineMatching()
	{
		MatchingWithMetric optimalMatchingWithMetric = ILPPPSolutionFor(projects).solution().orElseThrow();
		StudentProjectMaxFlowMatchings resultingMatching  = optimalMatchingWithMetric.matching;

		var result = new HashMap<Project, java.util.Collection<Group.FormedGroup>>();

		resultingMatching.groupedByProject().forEach((proj, agentList) -> {
			Agents agentsWithProject = new Agents(agentList);
			BepSysImprovedGroups bepSysImprovedGroups = new BepSysImprovedGroups(agentsWithProject, groupSizeConstraint, true);
			var groups = bepSysImprovedGroups.asCollection();

			result.put(proj, groups);
		});

		List<GroupToProjectMatch<Group.FormedGroup>> matchingsAsList = result.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.map(formedGroup -> new GroupToProjectMatch<>(formedGroup, entry.getKey()))
			)
			.collect(Collectors.toList());


		return new FormedGroupToProjecMatchings(matchingsAsList);
	}

	boolean canFormValidGroupsWithoutRemainders(StudentProjectMaxFlowMatchings matching, GroupSizeConstraint groupSizeConstraint)
	{
		var groupedByProject = matching.groupedByProject();

		// using exceptions for control flow, bepsys group forming is not as flexible yet to do otherwise...
		try {
			groupedByProject.forEach((proj, agentList) -> {
				Agents agentsWithProject = new Agents(agentList);
				BepSysImprovedGroups bepSysImprovedGroups = new BepSysImprovedGroups(agentsWithProject, groupSizeConstraint, true);
				var groups = bepSysImprovedGroups.asCollection();
			});
		}
		catch (RuntimeException ex) {
			return false;
		}

		return true;
	}

	final Object bestSoFarLock = new Object();
	float bestSoFar = 0;

	private final ConcurrentHashMap<Projects, ILPPPSolution> solutionsCache = new ConcurrentHashMap<>();

	private ILPPPSolution ILPPPSolutionFor(Projects projs)
	{
		return solutionsCache.computeIfAbsent(projs, ILPPPSolution::new);
	}

	class ILPPPSolution
	{
		private final Projects projects;

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		public Optional<MatchingWithMetric> solution = null;

		private ILPPPSolution(Projects projects)
		{
			// makes use of the parent instance's agents (ILPPPDeterminedMatchings)
			this.projects = projects;
		}

		public synchronized Optional<MatchingWithMetric> solution() {
			//noinspection OptionalAssignedToNull
			if (solution == null) {
				solution = solve(this::solutionIsAcceptable, projects, agents, groupSizeConstraint);
			}

			return solution;
		}

		private boolean solutionIsAcceptable(StudentProjectMaxFlowMatchings m)
		{
			boolean noProjectsWithStudentsButLessThanMinimumSize = m.groupedByProject().values().stream().allMatch(studentsAssignedToProject -> {
					// check if the following is true for each project
					var size = studentsAssignedToProject.size();
					return size >= groupSizeConstraint.minSize() || size == 0;
				}
			);

			return noProjectsWithStudentsButLessThanMinimumSize && m.allStudentsAreMatched() && canFormValidGroupsWithoutRemainders(m, groupSizeConstraint);
		}

		private Optional<MatchingWithMetric> solve(Predicate<StudentProjectMaxFlowMatchings> candidateSoltutionTest, Projects projects, Agents agents, GroupSizeConstraint groupSizeConstraint) {

			var matching = StudentProjectMaxFlowMatchings.of(agents, projects, groupSizeConstraint.maxSize());

			SingleGroupPerProjectMatchings singleGroup = new SingleGroupPerProjectMatchings(matching);
			var metric = new AUPCR.StudentAUPCR(singleGroup, projects, agents);

			synchronized (bestSoFarLock) {
				if (metric.result() <= bestSoFar) {
					// We don't have to explore solution further, it's less good
					return Optional.empty();
				}

				// this solution is >= bestSoFar, but is it a candidate solution?
				if (candidateSoltutionTest.test(matching) && canFormValidGroupsWithoutRemainders(matching, groupSizeConstraint)) {
					bestSoFar = metric.result();

					return Optional.of(new MatchingWithMetric(matching, metric));
				}
			}


			var matchingGroupedByProject = matching.groupedByProject();
			var equallyLeastPopularProjects = new EquallyLeastPopularProjects(matchingGroupedByProject, groupSizeConstraint.maxSize());

			var result = equallyLeastPopularProjects.asCollection()
				.parallelStream()
//				.stream()

				.map(equallyLeastPopularProject -> projects.without(equallyLeastPopularProject))

				.map(projectsWithoutLeastPopularProject -> ILPPPSolutionFor(projectsWithoutLeastPopularProject))

				.map(ILPPPSolution::solution)

				// discard empty optionals, unpack non-empty ones
				.flatMap(Optional::stream)

				// take best
				.max(Comparator.comparing(matchingWithMetric ->
					matchingWithMetric.metric.result()
				));

			return result;
		}
	}

	static class MatchingWithMetric
	{
		public final StudentProjectMaxFlowMatchings matching;
		public final AUPCR metric;

		public MatchingWithMetric(StudentProjectMaxFlowMatchings matching, AUPCR metric)
		{
			this.matching = matching;
			this.metric = metric;
		}
	}

}
