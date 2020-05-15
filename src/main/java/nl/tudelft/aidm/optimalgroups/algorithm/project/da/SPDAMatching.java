package nl.tudelft.aidm.optimalgroups.algorithm.project.da;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.jetbrains.annotations.NotNull;
import org.sql2o.GenericDatasource;
import plouchtch.assertion.Assert;
import plouchtch.lang.exception.ImplementMe;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SPDAMatching implements AgentToProjectMatching
{
	private final DatasetContext datasetContext;
	private final Agents agents;
	private final Projects projects;

	private List<Match<Agent, Project>> matchingOutcome;

	// for testing
	public static void main(String[] args)
	{
		var dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
		var matching = new SPDAMatching(CourseEdition.fromBepSysDatabase(dataSource, 4));

		var matches = matching.asList();

		for (var match : matches) {
			Agent student = match.from();
			Project project = match.to();
			var assignedProjectRank = new AssignedProjectRankStudent(student, project);

			int rankNumber = assignedProjectRank.asInt();
//			System.out.println("Group " + match.from().groupId() + " got project " + match.to().id() + " (ranked as number " + rankNumber + ")");

//			assignedProjectRank.studentRanks().forEach(metric -> {
			System.out.printf("Student %s\trank: %s\n", assignedProjectRank.student().id, assignedProjectRank.asInt());

//				System.out.printf("\t\t group satisfaction: %s\n", new GroupPreferenceSatisfaction(match, assignedProjectRank.student()).asFraction());
//			});
		}

		return;
	}

	public SPDAMatching(DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
		this.agents = datasetContext.allAgents();
		this.projects = datasetContext.allProjects();

		// todo: sanity check (capacity)
	}

	@Override
	public Map<Project, List<Agent>> groupedByProject()
	{
		throw new ImplementMe();
	}

	@Override
	public List<Match<Agent, Project>> asList()
	{
		if (matchingOutcome == null) {
			matchingOutcome = determine();
		}

		return matchingOutcome;
	}

	@Override
	public DatasetContext datasetContext()
	{
		return datasetContext;
	}

	private List<Match<Agent, Project>> determine()
	{
		var proposableProjects = new ProposableProjects(this.projects);
		var unmatched = new Stack<ProposingAgent>();

		Consumer<ProposingAgent> rejectionFn = stud -> {
//			System.out.printf("   Student %s rejected\n", stud.id);
			unmatched.add(stud);
		};

		Proposal.Template proposalTemplate = (proposingAgent, project) ->
			new Proposal(proposingAgent, project,
				proposal -> {}, // do nothing if accepted (ProposableProjects manage their tentatively accepted)
				proposal -> rejectionFn.accept(proposal.proposingAgent)
			);

		// Put agents into unmatched
		this.agents.asCollection().stream()
			.map(agent -> new ProposingAgent(agent, proposalTemplate))
			.forEach(unmatched::push);


		while (unmatched.size() > 0) {
			var unmatchedAgent = unmatched.pop();
			var proposal = unmatchedAgent.makeNextProposal();
//			System.out.printf("Student %s,\tproposing to: %s\n", unmatchedAgent.agent, proposal.projectProposingFor().id());
			proposableProjects.receiveProposal(proposal);
		}

		/* Algo done, now transform into a Matching */
		List<Match<Agent, Project>> matching = new ArrayList<>();
		for (ProposableProject proposableProject : proposableProjects)
		{
			proposableProject.acceptedAgents().forEach(agent -> {
				var match = new AgentToProjectMatch(agent, proposableProject.project);
				matching.add(match);
			});
		}

		Assert.that(matching.stream().map(Match::from).distinct().count() == datasetContext.allAgents().count())
			.orThrowMessage("Not all agents matched");

		return matching;
	}

	static class ProposableProjects implements Iterable<ProposableProject>
	{
		private final List<ProposableProject> asList;

		ProposableProjects(Projects projects)
		{
			this.asList = projects.asCollection().stream()
				.map(ProposableProject::new)
				.collect(Collectors.toList());
		}

		void receiveProposal(Proposal proposal)
		{
			// UGLY and inefficient: FIXME!
			var projToProposeTo = asList.stream().filter(proposableProject -> proposableProject.id() == proposal.projectProposingFor().id())
				.findAny().get();

			projToProposeTo.handleProposal(proposal);
		}

		@NotNull
		@Override
		public Iterator<ProposableProject> iterator()
		{
			return asList.iterator();
		}
	}

}
