package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.pref.AggregatedProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ChiarandiniGroupToProjectMatching implements GroupToProjectMatching<Group.FormedGroup>
{
	private final DatasetContext datasetContext;
	private final List<Match<Group.FormedGroup, Project>> asList;
	
	public ChiarandiniGroupToProjectMatching(AssignmentConstraints.XVars xVars, DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
		
		var asMap = new IdentityHashMap<Project.ProjectSlot, List<Agent>>();
		
		Function<Project.ProjectSlot, List<Agent>> emptyListIfAbsent = (x) -> new ArrayList<>();
		
		datasetContext.allAgents().forEach(agent ->
		{
			datasetContext.allProjects().forEach(project ->
			{
				project.slots().forEach(slot ->
				{
					xVars.of(agent, slot).ifPresent(x ->
					{
						var xValue = x.getValueOrThrow();

						if (xValue > 0.9)
						{
							asMap.computeIfAbsent(slot, emptyListIfAbsent)
									.add(agent);
						}
					});
				});
			});
		});
		
		var index = new AtomicInteger(1);
		var asList = new ArrayList<Match<Group.FormedGroup, Project>>(asMap.size());
		
		asMap.forEach((projectSlot, agents) -> {
			var members = Agents.from(agents);
			var aggProjectPreferences = AggregatedProjectPreference.usingGloballyConfiguredMethod(members);
			
			var grp = new Group.FormedGroup(members, aggProjectPreferences, index.getAndIncrement());
			asList.add(new GroupToProjectMatch<>(grp, projectSlot.belongingToProject()));
		});
		
		this.asList = asList;
	}
	
	@Override
	public List<Match<Group.FormedGroup, Project>> asList()
	{
		return asList;
	}
	
	@Override
	public DatasetContext datasetContext()
	{
		return this.datasetContext;
	}
}
