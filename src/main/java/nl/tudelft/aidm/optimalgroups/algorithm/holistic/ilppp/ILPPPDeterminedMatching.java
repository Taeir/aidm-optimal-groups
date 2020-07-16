package nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp;

import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.BepSysReworked;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.SetOfGroupSizesThatCanStillBeCreated;
import nl.tudelft.aidm.optimalgroups.metric.rank.AssignedRank;
import nl.tudelft.aidm.optimalgroups.model.matching.*;
import nl.tudelft.aidm.optimalgroups.algorithm.project.AgentProjectMaxFlowMatching;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.metric.matching.group.PeerPreferenceSatisfaction;
import nl.tudelft.aidm.optimalgroups.model.*;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.sql2o.GenericDatasource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ILPPPDeterminedMatching implements GroupToProjectMatching<Group.FormedGroup>
{
	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;
	private final DatasetContext datasetContext;

	private FormedGroupToProjectMatching matchings = null;

	// for testing
	public static void main(String[] args)
	{
		var dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
		var matching = new ILPPPDeterminedMatching(CourseEdition.fromBepSysDatabase(dataSource, 10));

		List<Match<Group.FormedGroup, Project>> matches = matching.asList();

		for (var match : matches) {
			AssignedRank.ProjectToGroup assignedProjectRank = new AssignedRank.ProjectToGroup(match);

			int rankNumber = assignedProjectRank.asInt().orElse(-1);
			System.out.println("Group " + match.from().groupId() + " got project " + match.to().id() + " (ranked as number " + rankNumber + ")");

			assignedProjectRank.studentRanks().forEach(metric -> {
				System.out.printf("\t\t-Student %s", metric.student().id);
				System.out.printf(", rank: %s", metric.asInt());

				System.out.printf("\t\t group satisfaction: %s\n", new PeerPreferenceSatisfaction(match.from(), metric.student()).asFraction());
			});
		}

		return;
	}

	public ILPPPDeterminedMatching(DatasetContext datasetContext)
	{
		this.agents = datasetContext.allAgents();
		this.projects = datasetContext.allProjects();
		this.groupSizeConstraint = datasetContext.groupSizeConstraint();
		this.datasetContext = datasetContext;
	}

	@Override
	public DatasetContext datasetContext()
	{
		return datasetContext;
	}

	@Override
	public List<Match<Group.FormedGroup, Project>> asList()
	{
		if (matchings == null) {
			matchings = determineMatching();
		}

		return matchings.asList();
	}

	public FormedGroupToProjectMatching determineMatching()
	{
		var optimalMatchingWithMetric = ILPPPSolutionFor(projects).solution().orElseThrow();
		var resultingMatching  = optimalMatchingWithMetric.matching;

		var result = new HashMap<Project, java.util.Collection<Group.FormedGroup>>();

		resultingMatching.groupedByProject().forEach((proj, agentList) -> {
			Agents agentsWithProject = Agents.from(agentList);
			BepSysImprovedGroups bepSysImprovedGroups = new BepSysImprovedGroups(agentsWithProject, groupSizeConstraint, true);
			var groups = bepSysImprovedGroups.asCollection();

			result.put(proj, groups);
		});

		List<GroupToProjectMatch<Group.FormedGroup>> matchingsAsList = result.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.map(formedGroup -> new GroupToProjectMatch<>(formedGroup, entry.getKey()))
			)
			.collect(Collectors.toList());


		return new FormedGroupToProjectMatching(datasetContext, matchingsAsList);
	}

	boolean canFormValidGroupsWithoutRemainders(AgentProjectMaxFlowMatching matching, GroupSizeConstraint groupSizeConstraint)
	{
		var groupedByProject = matching.groupedByProject();

		return groupedByProject.entrySet().stream()
				.allMatch(entry -> {
					var agentList = entry.getValue();
					Agents agentsWithProject = Agents.from(agentList);

					// force evaulation of the lazy
					try {
						new SetOfGroupSizesThatCanStillBeCreated(agentList.size(), groupSizeConstraint, SetOfGroupSizesThatCanStillBeCreated.FocusMode.MAX_FOCUS);
						return new BepSysReworked(agentsWithProject, groupSizeConstraint).asFormedGroups().countDistinctStudents() == agentList.size();
//						return true;
					}
					catch (Exception ex) {
						return false;
					}
				});
	}

	final Object bestSoFarLock = new Object();
	double bestSoFar = 0;

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
				solution = solve(this::solutionIsAcceptable, groupSizeConstraint);
			}

			return solution;
		}

		private boolean solutionIsAcceptable(AgentProjectMaxFlowMatching m)
		{
			boolean noProjectsWithoutStudentsOrWithLessThanMinimumSize = m.groupedByProject().values().stream().allMatch(studentsAssignedToProject -> {
					// check if the following is true for each project
					var size = studentsAssignedToProject.size();
					return size >= groupSizeConstraint.minSize() || size == 0;
				}
			);

			return noProjectsWithoutStudentsOrWithLessThanMinimumSize && m.allStudentsAreMatched() && canFormValidGroupsWithoutRemainders(m, groupSizeConstraint);
		}

		private Optional<MatchingWithMetric> solve(Predicate<AgentProjectMaxFlowMatching> candidateSoltutionTest, GroupSizeConstraint groupSizeConstraint) {

			var matching = AgentProjectMaxFlowMatching.of(datasetContext, agents, projects);

//			SingleGroupPerProjectMatching singleGroup = new SingleGroupPerProjectMatching(matching);
			var metric = new AUPCRStudent(matching, projects, agents);

			synchronized (bestSoFarLock) {
				if (metric.asDouble() <= bestSoFar) {
					// We don't have to explore solution further, it's less good
					return Optional.empty();
				}

				// this solution is >= bestSoFar, but is it a candidate solution?
				if (candidateSoltutionTest.test(matching) && canFormValidGroupsWithoutRemainders(matching, groupSizeConstraint)) {
					bestSoFar = metric.asDouble();

					var matchingWithProperDatasetContext = new StudentProjectILPPPPMatching(matching);
					return Optional.of(new MatchingWithMetric(matchingWithProperDatasetContext, metric));
				}
			}


			var matchingGroupedByProject = matching.groupedByProject();
			var equallyLeastPopularProjects = new EquallyLeastPopularProjects(datasetContext, matchingGroupedByProject);

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
					matchingWithMetric.metric.asDouble()
				));

			return result;
		}
	}

	static class MatchingWithMetric
	{
		public final StudentProjectILPPPPMatching matching;
		public final AUPCR metric;

		public MatchingWithMetric(StudentProjectILPPPPMatching matching, AUPCR metric)
		{
			this.matching = matching;
			this.metric = metric;
		}
	}

	class StudentProjectILPPPPMatching implements AgentToProjectMatching
	{
		private final AgentToProjectMatching underlying;

		public StudentProjectILPPPPMatching(AgentToProjectMatching underlying)
		{
			this.underlying = underlying;
		}

		@Override
		public List<Match<Agent, Project>> asList()
		{
			return underlying.asList();
		}

		@Override
		public DatasetContext datasetContext()
		{
			// from the parent class
			return datasetContext;
		}

		@Override
		public Map<Project, List<Agent>> groupedByProject()
		{
			return underlying.groupedByProject();
		}
	}

	static class ILPPPDataContext implements DatasetContext
	{
		private final DatasetContext original;
		private final Projects projects;
		private final Agents agents;
		private final GroupSizeConstraint groupSizeConstraint;

		public ILPPPDataContext(DatasetContext original, Projects projects, Agents agents, GroupSizeConstraint groupSizeConstraint)
		{
			this.original = original;
			this.projects = projects;
			this.agents = agents;
			this.groupSizeConstraint = groupSizeConstraint;
		}

		/**
		 * The original DatasetContext is the one used to start the ILPPP algorithm. As the ILPPP iteratively removes projects,
		 * the dataset is updated. A good question is whether ILPPP needs to have its own DatasetContext subtype and if using it this
		 * way adheres to the core idea of a DatasetContext... as the context is the same but we're simply choosing to not consider
		 * certain projects anymore... TODO
		 * @return
		 */
		public DatasetContext originalDatasetContext()
		{
			return original;
		}

		@Override
		public String identifier()
		{
			return String.format("DC-ILPPP[p%s(%s)_a%s(%s)]_GSC%s", projects.count(), projects.hashCode(), agents.count(), agents.hashCode(), groupSizeConstraint);
		}

		@Override
		public Projects allProjects()
		{
			return projects;
		}

		@Override
		public Agents allAgents()
		{
			return agents;
		}

		@Override
		public GroupSizeConstraint groupSizeConstraint()
		{
			return groupSizeConstraint;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof ILPPPDataContext)) return false;
			ILPPPDataContext that = (ILPPPDataContext) o;
			return projects.equals(that.projects) &&
				agents.equals(that.agents) &&
				groupSizeConstraint.equals(that.groupSizeConstraint);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(projects, agents, groupSizeConstraint);
		}
	}

}
