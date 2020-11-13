package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.model.Edge;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class ProjectEdges
{
	private Set<Agent>[] edgesPerRankBucket;
	private Project project;

	public ProjectEdges(int expectedMaxRank, Project project)
	{
		this.edgesPerRankBucket = new Set[expectedMaxRank + 1];// ArrayList<>(expectedMaxRank);
		this.project = project;
	}

	public void add(Edge edge)
	{
		var bin = edgesPerRankBucket[edge.rank()];
		if (bin == null)
		{
			bin = new HashSet<>();
			edgesPerRankBucket[edge.rank()] = bin;
		}

		bin.add(edge.from());
	}

	public Optional<ProjectAgentsPairing> pairingForProject(int rankBoundInclusive, GroupSizeConstraint groupSizeConstraint)
	{
		var potentialGroupmates = new HashSet<Agent>();

		// Indifferent agents are potential group mates of everyone
		if (edgesPerRankBucket[0] != null)
		{
			potentialGroupmates.addAll(edgesPerRankBucket[0]);
		}

		for (int rank = 1; rank <= rankBoundInclusive; rank++)
		{
			var agentsWithRank = edgesPerRankBucket[rank];

			if (agentsWithRank == null) continue;

			if (agentsWithRank.size() + potentialGroupmates.size() >= groupSizeConstraint.minSize())
			{
				// We've reached the "pivot point", so the 'rank' is the 'k'
				return Optional.of(new ProjectAgentsPairing(rank, project, agentsWithRank, potentialGroupmates));
			}

			potentialGroupmates.addAll(agentsWithRank);
		}

		return Optional.empty();
	}
}
