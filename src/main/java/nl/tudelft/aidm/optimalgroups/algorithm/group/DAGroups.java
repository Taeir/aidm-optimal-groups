package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.algorithm.generic.da.*;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.group.LeastWorstIndividualRankAttainableInGroup;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import org.sql2o.GenericDatasource;
import plouchtch.lang.exception.ImplementMe;

import java.util.*;
import java.util.stream.Collectors;

public class DAGroups
{
	private final Agents agents;
	private final GroupSizeConstraint groupSizeConstraint;

	public static void main(String[] args)
	{
		var dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
		var ce = CourseEdition.fromBepSysDatabase(dataSource, 10);
		var da = new DAGroups(ce.allAgents(), ce.groupSizeConstraint());
		da.bla();
	}

	public DAGroups(Agents agents, GroupSizeConstraint groupSizeConstraint)
	{
		this.agents = agents;
		this.groupSizeConstraint = groupSizeConstraint;
	}

	List<Match<ParticipatingAgent, ParticipatingAgent>> mechanism()
	{
		var participatingAgents = agents.asCollection().stream()
			.map(agent -> new ParticipatingAgent(agent, agents))
			.collect(Collectors.toList());

		var unmatched = new Stack<ParticipatingAgent>();
		unmatched.addAll(participatingAgents);

		// PHASE 1: ensure no single agents
		while (unmatched.size() > 0) {
			var unmatchedProposer = unmatched.pop();
			var proposal = unmatchedProposer.makeNextProposal();

//			System.out.printf("Student %s,\tproposing to: %s\n", unmatchedProposer.agent, proposal.projectProposingFor().id());
			var answer = proposables.receiveProposal(proposalTemplate.apply(proposal));
			switch (answer) {
				case TentivelyAccept:
			}
		}

		// PHASE 2: ensure all pairs and groups are merged into valid groups

	}

	public void bla()
	{

		var matching = new Mechanism<>(proposers, proposables);

		var pairs = matching.determine()
			.stream()
			.map(match -> Set.of(match.from(), match.to()))
			.collect(Collectors.toList());

		boolean henk = false;
		var map = new HashMap<Agent, Set<Agent>>();
		while(!pairs.isEmpty())
		{
			var pair = pairs.remove(0).toArray(Agent[]::new);
			var g1 = map.getOrDefault(pair[0], Collections.emptySet());
			var g2 = map.getOrDefault(pair[1], Collections.emptySet());

			var newGroup = new HashSet<Agent>();
			newGroup.addAll(g1);
			newGroup.addAll(g2);

			for (var member : Set.copyOf(newGroup)) {
				Set<Agent> groupSoFarOfMember = map.getOrDefault(member, Collections.emptySet());
				newGroup.addAll(groupSoFarOfMember);
			}

			for (var member : newGroup) {
				map.put(member, newGroup);
			}
		}

		return;
	}

	public static class ParticipatingAgent implements Proposer<Agent, Agent>, Proposable<Agent,Agent>
	{
		private final Agent underlying;
		private final Agents universe;

		private final List<ParticipatingAgent> possibleGroup;

		PriorityQueue<PotentialGroupMate> peerRanking;

		public ParticipatingAgent(Agent underlying, Agents universe)
		{
			this.underlying = underlying;
			this.universe = universe;

			this.possibleGroup = new ArrayList<>();
			this.possibleGroup.add(this);

			this.peerRanking = new PriorityQueue<>();

			universe.asCollection().stream()
				.filter(agent -> !agent.equals(underlying))
				.map(otherAgent -> {
					LeastWorstDistance distanceThisToThatAgent = new LeastWorstDistance(possibleGroup);
					return new PotentialGroupMate(underlying, otherAgent, distanceThisToThatAgent);
				})
				.forEach(peerRanking::add);
		}

		public List<ParticipatingAgent> currentGroup()
		{
			return Collections.unmodifiableList(possibleGroup);
		}

		@Override
		public Proposal<Agent, Agent> makeNextProposal()
		{
			var next = peerRanking.poll();
			var after = peerRanking.peek();
			var utilityIfProposalRejected = after != null ?
				after.distance.leastWorstRank()
				: Integer.MAX_VALUE;

			return new Proposal<Agent, Agent>()
			{
				@Override
				public Agent proposer()
				{
					return underlying;
				}

				@Override
				public Agent recipient()
				{
					return next.possibleGroupMember;
				}

				@Override
				public Integer utilityIfAccepted()
				{
					return next.distance.leastWorstRank();
				}

				@Override
				public Integer agentsExpectedUtilityAfterReject()
				{
					return utilityIfProposalRejected;
				}
			};
		}

		@Override
		public void handleProposal(Proposal.Actionable<Agent, Agent> proposal)
		{
			throw new ImplementMe();
		}

		@Override
		public Collection<Agent> accepted()
		{
			throw new ImplementMe();
		}

		@Override
		public Agent subject()
		{
			return underlying;
		}

		@Override
		public Agent underlying()
		{
			return underlying;
		}
	}

	static class PotentialGroupMate implements Comparable<PotentialGroupMate>
	{
		Agent self;
		Agent possibleGroupMember;
		LeastWorstDistance distance;

		public PotentialGroupMate(Agent self, Agent potentialOther, LeastWorstDistance distance)
		{
			this.self = self;
			this.possibleGroupMember = potentialOther;
			this.distance = distance;
		}

		@Override
		public int compareTo(DAGroups.PotentialGroupMate o)
		{
			return this.distance.compareTo(o.distance);
		}
	}


	public interface ProjectPreferenceDistance<T> extends Comparable<T>
	{
	}

	public static class LeastWorstDistance implements ProjectPreferenceDistance<LeastWorstDistance>
	{
		private final Agents agents;
		private Integer leastWorstRankResult = null;

		public LeastWorstDistance(Collection<ParticipatingAgent> agents)
		{
			List<Agent> agentsCollection = agents.stream().map(participatingAgent -> participatingAgent.underlying).collect(Collectors.toList());
			this.agents = Agents.from(agentsCollection);
		}

		private Integer leastWorstRank()
		{
			if (leastWorstRankResult == null) {
				leastWorstRankResult = new LeastWorstIndividualRankAttainableInGroup(agents)
					.asInt().orElseThrow();
			}

			return leastWorstRankResult;
		}

		@Override
		public int compareTo(LeastWorstDistance other)
		{
//			Assert.that(this.agents.equals(other.agents))
//				.orThrowMessage("Can only compare distances that start from the same point");

			return this.leastWorstRank().compareTo(other.leastWorstRank());
		}
	}
}
