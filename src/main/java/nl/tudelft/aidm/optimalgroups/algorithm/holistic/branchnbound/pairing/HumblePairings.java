package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.model.Edge;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Finds all "humble" pairings for given projects and agents
 */
public class HumblePairings
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
	public HumblePairings(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint)
	{
		this(agents, projects, groupSizeConstraint, projects.count());
	}

	/**
	 * @param rankBound The upperbound (incl) for the worst rank achieved among agents per pairing
	 * @param agents The agents
	 * @param projects The available projects
	 * @param groupSizeConstraint The groupsize constraint
	 */
	public HumblePairings(Agents agents, Projects projects, GroupSizeConstraint groupSizeConstraint, int rankBound)
	{
		Assert.that(/*agents.count() == 0 || */agents.count() >= groupSizeConstraint.minSize())
			.orThrowMessage("Cannot determine pairings: given agents cannot even constitute a min-size group");

		this.agents = agents;
		this.projects = projects;
		this.groupSizeConstraint = groupSizeConstraint;
		this.rankBound = rankBound;
	}

	public Stream<ProjectAgentsPairing> asStream()
	{
		return projects.asCollection().stream()
			.map(this::forProject)
			.flatMap(Optional::stream);
	}

	private Optional<ProjectAgentsPairing> forProject(Project project)
	{
		final var edgesToProject = new ProjectEdges(rankBound, project);

		agents.forEach(agent -> {
			var rank = agent.projectPreference().rankOf(project)
				.orElse(0); // indifferent agents are ok with everything

			if (rank <= rankBound) {
				var edge = new Edge(agent, project, rank);

				edgesToProject.add(edge);
			}
		});

		return edgesToProject.pairingForProject(rankBound, groupSizeConstraint);
	}

}
