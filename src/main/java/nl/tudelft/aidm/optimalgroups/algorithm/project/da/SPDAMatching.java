package nl.tudelft.aidm.optimalgroups.algorithm.project.da;

import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMatching;
import nl.tudelft.aidm.optimalgroups.metric.matching.rankofassigned.AssignedProjectRankStudent;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.match.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.sql2o.GenericDatasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SPDAMatching implements StudentProjectMatching
{
	private final CourseEdition courseEdition;
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

			int rankNumber = assignedProjectRank.studentsRank();
//			System.out.println("Group " + match.from().groupId() + " got project " + match.to().id() + " (ranked as number " + rankNumber + ")");

//			assignedProjectRank.studentRanks().forEach(metric -> {
			System.out.printf("Student %s\trank: %s\n", assignedProjectRank.student().id, assignedProjectRank.studentsRank());

//				System.out.printf("\t\t group satisfaction: %s\n", new GroupPreferenceSatisfaction(match, assignedProjectRank.student()).asFraction());
//			});
		}

		return;
	}

	public SPDAMatching(CourseEdition courseEdition)
	{
		this.courseEdition = courseEdition;
		this.agents = courseEdition.allAgents();
		this.projects = courseEdition.allProjects();

		// todo: sanity check (capacity)
	}

//	public static SPDAMatching of(Agents agents, Projects projects)
//	{
//		return new SPDAMatching(agents, projects);
//	}

	@Override
	public Map<Project, List<Agent>> groupedByProject()
	{
		return null;
	}

	@Override
	public List<Match<Agent, Project>> asList()
	{
		if (matchingOutcome == null) {
			matchingOutcome = determine();
		}

		return matchingOutcome;
	}

	private List<Match<Agent, Project>> determine()
	{
		List<ProposingAgent> unmatched = this.agents.asCollection().stream().map(ProposingAgent::new).collect(Collectors.toList());
		List<ProposingAgent> matched = new ArrayList<>(unmatched.size());

		Consumer<ProposingAgent> rejectionHandler = stud -> {
			System.out.printf("   Student %s rejected\n", stud.id);
			unmatched.add(stud);
		};
		Collection<ProposableProject> proposableProjects = projects.asCollection().stream().map((Project project) -> new ProposableProject(project, rejectionHandler)).collect(Collectors.toList());

		Predicate<Collection<?>> notEmpty = (collection) -> collection.isEmpty() == false;

		while (notEmpty.test(unmatched)) {
			var unmatchedAgent = unmatched.remove(0);

			// propose to
			var proposal = unmatchedAgent.makeNextProposal();

			System.out.printf("Student %s,\tproposing to: %s\n", unmatchedAgent.agent, proposal.projectProposingFor().id());

			// UGLY and inefficient: FIXME!
			var projToProposeTo = proposableProjects.stream().filter(proposableProject -> proposableProject.id() == proposal.projectProposingFor().id())
				.findAny().get();

			// note: exception (stack is empty or an npe) --> there are no projects to propose to
			if (projToProposeTo.receiveProposal(proposal) == ProposableProject.ProposalAnswer.REJECT) {
				unmatched.add(unmatchedAgent);
			}
		}

		List<Match<Agent, Project>> matching = new ArrayList<>();
		for (ProposableProject proposableProject : proposableProjects)
		{
			proposableProject.acceptedAgents().forEach(agent -> {
				var match = new AgentToProjectMatch(agent, proposableProject.project);
				matching.add(match);
			});
		}

		return matching;
	}

}
