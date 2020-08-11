package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism.GroupFactorization;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.pref.AggregatedProfilePreference;
import plouchtch.assertion.Assert;
import plouchtch.lang.exception.ImplementMe;

import java.util.*;
import java.util.function.Consumer;

public class TrivialGroupPartitioning extends Groups.ListBacked<Group.FormedGroup> implements GroupFormingAlgorithm
{
	private final Agents agents;
	private final GroupFactorization groupFactorization;

	private FormedGroups groups;

	public TrivialGroupPartitioning(Agents agents)
	{
		this(agents, GroupFactorization.sharedInstance(agents.datsetContext.groupSizeConstraint()));
	}

	public TrivialGroupPartitioning(Agents agents, GroupFactorization groupFactorization)
	{
		this.agents = agents;
		this.groupFactorization = groupFactorization;
		this.groups = null;
	}

	@Override
	protected List<Group.FormedGroup> asList()
	{
		if (groups == null) {
			var groupsTemp = new FormedGroups();

			var gsc = agents.datsetContext.groupSizeConstraint();
			var factorization = groupFactorization.forGivenNumberOfStudents(agents.count());

			Assert.that(factorization.isFactorable())
				.orThrowMessage(String.format(
					"Given number of students (%s) is not factorable into groups of sizes between (%s) and (%s)",
					agents.count(), gsc.minSize(), gsc.maxSize())
				);

			var numGroupsMaxSize = factorization.numGroupsOfMaxSize();
			var numGroupsMinSize = factorization.numGroupsOfMinSize();

			var agentsCopyToShuffled = new ArrayList<>(agents.asCollection());
			Collections.shuffle(agentsCopyToShuffled);

			var agentsToGroup = new Stack<Agent>();
			agentsToGroup.addAll(agentsCopyToShuffled);

			var maxSize = gsc.maxSize();
			for (int i = 0; i < numGroupsMaxSize; i++)
			{
				var group = new ArrayList<Agent>();
				for (int j = 0; j < maxSize; j++)
				{
					group.add(agentsToGroup.pop());
				}

				var groupAsAgents = Agents.from(group);
				var projPrefs = AggregatedProfilePreference.usingGloballyConfiguredMethod(groupAsAgents);
				var tentativeGroup = new Group.TentativeGroup(Agents.from(group), projPrefs);
				groupsTemp.addAsFormed(tentativeGroup);
			}

			var minSize = gsc.minSize();
			for (int i = 0; i < numGroupsMinSize; i++)
			{
				var group = new ArrayList<Agent>();
				for (int j = 0; j < minSize; j++)
				{
					group.add(agentsToGroup.pop());
				}

				var groupAsAgents = Agents.from(group);
				var projPrefs = AggregatedProfilePreference.usingGloballyConfiguredMethod(groupAsAgents);
				var tentativeGroup = new Group.TentativeGroup(Agents.from(group), projPrefs);
				groupsTemp.addAsFormed(tentativeGroup);
			}

			Assert.that(agentsToGroup.isEmpty())
				.orThrowMessage("All agents should have been grouped now...");

			groups = groupsTemp;
		}

		return new ArrayList<>(groups.asCollection());
	}

	@Override
	public FormedGroups asFormedGroups()
	{
		return this.groups;
	}
}
