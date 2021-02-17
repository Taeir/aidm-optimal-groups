package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model.MatchCandidate;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Finds all "humble" pairings for given projects and agents
 */
public class BestHumblePairings
{
	private final Agents agents;
	private final Projects projects;
	private final MinQuorumRequirement minQuorumRequirement;
	private final int rankBound;


	/**
	 * @param agents The agents
	 * @param projects The available projects
	 * @param minQuorumRequirement
	 */
	public BestHumblePairings(Agents agents, Projects projects, MinQuorumRequirement minQuorumRequirement)
	{
		this(agents, projects, minQuorumRequirement, projects.count());
	}

	/**
	 * @param rankBound The upperbound (incl) for the worst rank achieved among agents per pairing
	 * @param agents The agents
	 * @param projects The available projects
	 * @param minQuorumRequirement
	 */
	public BestHumblePairings(Agents agents, Projects projects, MinQuorumRequirement minQuorumRequirement, int rankBound)
	{
		var atLeastOneProjectCanReachMinimumQuorum = projects.asCollection().stream()
			.map(minQuorumRequirement::forProject)
			.anyMatch(numAgentsTillQuorum -> agents.count() >= numAgentsTillQuorum.asInt());
		
		Assert.that(/*agents.count() == 0 || */atLeastOneProjectCanReachMinimumQuorum)
			.orThrowMessage("Cannot determine pairings: given agents cannot even constitute a min-size group");


		this.agents = agents;
		this.projects = projects;
		this.minQuorumRequirement = minQuorumRequirement;
		this.rankBound = rankBound;
	}

	public Stream<MatchCandidate> asStream()
	{
		return projects.asCollection().stream()
			.map(this::forProject)
			.flatMap(Optional::stream);
	}

	public Optional<MatchCandidate> forProject(Project project)
	{
		final var edgesToProject = new ProjectDesirability(rankBound, project);

		agents.forEach(agent -> {
			var rank = agent.projectPreference().rankOf(project);
			if (rank.unacceptable()) return;

			// indifferent agents are ok with everything
			var rankInt = rank.isCompletelyIndifferent() ? 0 : rank.asInt();

			if (rankInt <= rankBound) {
				var edge = new ProjectDesirability.AgentsProjDesirability(agent, project, rankInt);

				edgesToProject.add(edge);
			}
		});

		return edgesToProject.pairingForProject(rankBound, minQuorumRequirement.forProject(project));
	}

}
