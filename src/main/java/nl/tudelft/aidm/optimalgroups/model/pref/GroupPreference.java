package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import plouchtch.lang.Lazy;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface GroupPreference
{
	// Array of id's sufficies for now
	int[] asArray();

	List<Agent> asListOfAgents();

	Integer count();
	
	Agent owner();
	
    /**
	 * Is the group proposed by the agent mutual? That is, do the agents
	 * that form part of the proposal have identical proposal? A.k.a. "a clique" of friends
	 * @return True if "friends" have 'identical' peer preferences
	 */
	default boolean isMutual()
	{
		if(this.count() == 0) {
			// No preference, therefore also no clique
			return false;
		}

		// Function that maps the Agent's "group preferences" to a Set of agents (including himself)
		// intuitively, this is the agent's proposal for a (partial) group formation.
		Function<Agent, Set<Agent>> agentPreferencesToProposedGroup = (Agent x) -> {
			var groupProposal = new HashSet<Agent>();
			//Add agent himself to set to make comparing preferences easy
			groupProposal.add(x);
			groupProposal.addAll(x.groupPreference.asListOfAgents());

			return groupProposal;
		};

		// The proposal of the given agent
		var proposedGroupOfAgent = agentPreferencesToProposedGroup.apply(owner());

		// A group proposal is completely  mutual if all agents in the proposal
		// would propose exactly the same proposal
		var agentProposalIsCompletelyMutual = this.asListOfAgents().stream()
			// Now check if the proposals of the peers, that are part of this agents proposal
			.map(agentPreferencesToProposedGroup)
			// are exactly the same as the proposal of "this" agent
			.allMatch(proposedGroupOfAgent::equals);

		return agentProposalIsCompletelyMutual;
	}

	/* */
	class None implements GroupPreference
	{
		private static int[] asArray = new int[0];
		
		private Lazy<Agent> owner;
		public None(Agent owner)
		{
			this.owner = new Lazy<>(() -> owner);
		}
		
		public None(Supplier<Agent> ownerSupplier)
		{
			this.owner = new Lazy<>(ownerSupplier);
		}

		@Override
		public int[] asArray()
		{
			return asArray;
		}

		@Override
		public List<Agent> asListOfAgents()
		{
			return Collections.emptyList();
		}

		@Override
		public Integer count()
		{
			return 0;
		}
		
		@Override
		public Agent owner()
		{
			return owner.get();
		}
	}
}
