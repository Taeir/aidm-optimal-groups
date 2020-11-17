package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model.MatchCandidate;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.*;

class ProjectDesirability
{
	private List<Agent>[] edgesPerRankBucket;
	private Project project;

	public ProjectDesirability(int expectedMaxRank, Project project)
	{
		this.edgesPerRankBucket = new LinkedList[expectedMaxRank + 1];// ArrayList<>(expectedMaxRank);
		this.project = project;
	}

	public void add(AgentsProjDesirability agentsProjDesirability)
	{
		var bin = edgesPerRankBucket[agentsProjDesirability.rank()];
		if (bin == null)
		{
			bin = new LinkedList<>();
			edgesPerRankBucket[agentsProjDesirability.rank()] = bin;
		}

		bin.add(agentsProjDesirability.from());
	}

	public Optional<MatchCandidate> pairingForProject(int rankBoundInclusive, GroupSizeConstraint groupSizeConstraint)
	{
		var potentialGroupmates = new LinkedList<Agent>();

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
				// We've reached the "pivot point"
				var agentsWithRankInSet = new HashSet<>(agentsWithRank);
				var potentialGroupmatesInSet = new HashSet<>(potentialGroupmates);

				MatchCandidate matchCandidate = new MatchCandidate(rank, project, agentsWithRankInSet, potentialGroupmatesInSet);
				return Optional.of(matchCandidate);
			}

			potentialGroupmates.addAll(agentsWithRank);
		}

		return Optional.empty();
	}

	public List<MatchCandidate> allPairingsForProject(int rankBoundInclusive, GroupSizeConstraint groupSizeConstraint)
	{
		var results = new LinkedList<MatchCandidate>();

		var potentialGroupmates = new LinkedList<Agent>();

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
				// We've reached the "pivot point"
				var agentsWithRankInSet = new HashSet<>(agentsWithRank);
				var potentialGroupmatesInSet = new HashSet<>(potentialGroupmates);

				var pairing =  new MatchCandidate(rank, project, agentsWithRankInSet, potentialGroupmatesInSet);
				results.add(pairing);
			}

			potentialGroupmates.addAll(agentsWithRank);
		}

		return results;
	}

	public static record AgentsProjDesirability(Agent from, Project to, int rank)
	{

	}
}
