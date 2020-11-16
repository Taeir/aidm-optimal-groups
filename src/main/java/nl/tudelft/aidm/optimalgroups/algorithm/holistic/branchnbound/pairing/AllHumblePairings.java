package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.model.MatchCandidate;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.util.stream.Stream;

/**
 * Finds all "humble" pairings for given projects and agents
 */
public class AllHumblePairings
{
	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;
	private final int rankBound;


	/**
	 * @param agents The agents
	 * @param projects The available projects
	 * @param groupSizeConstraint The groupsize constraint
	 */
	public AllHumblePairings(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		this(agents, projects, groupSizeConstraint, projects.count());
	}

	/**
	 * @param rankBound The upperbound (incl) for the worst rank achieved among agents per pairing
	 * @param agents The agents
	 * @param projects The available projects
	 * @param groupSizeConstraint The groupsize constraint
	 */
	public AllHumblePairings(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint, int rankBound)
	{
		Assert.that(/*agents.count() == 0 || */agents.count() >= groupSizeConstraint.minSize())
			.orThrowMessage("Cannot determine pairings: given agents cannot even constitute a min-size group");

		this.agents = agents;
		this.projects = projects;
		this.groupSizeConstraint = groupSizeConstraint;
		this.rankBound = rankBound;
	}

	public Stream<MatchCandidate> asStream()
	{
		return projects.asCollection().stream()
			.flatMap(this::forProject);
	}

	private Stream<MatchCandidate> forProject(Project project)
	{
		final var edgesToProject = new ProjectDesirability(rankBound, project);

		agents.forEach(agent -> {
			var rank = agent.projectPreference().rankOf(project)
				.orElse(0); // indifferent agents are ok with everything

			if (rank <= rankBound) {
				var edge = new ProjectDesirability.AgentsProjDesirability(agent, project, rank);

				edgesToProject.add(edge);
			}
		});

		return edgesToProject.allPairingsForProject(rankBound, groupSizeConstraint).stream();
	}

}
