package nl.tudelft.aidm.optimalgroups.model.matching;

import nl.tudelft.aidm.optimalgroups.algorithm.group.TrivialGroupPartitioning;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.BepSysReworked;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Group.FormedGroup;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FormedGroupToProjectMatching extends ListBasedMatching<FormedGroup, Project> implements GroupToProjectMatching<FormedGroup>
{
	public FormedGroupToProjectMatching(DatasetContext datasetContext, List<? extends Match<FormedGroup, Project>> list)
	{
		super(datasetContext, (List<Match<FormedGroup, Project>>) list);
	}

	public static FormedGroupToProjectMatching byTriviallyPartitioning(AgentToProjectMatching agentToProjectMatching)
	{
		var datasetContext = agentToProjectMatching.datasetContext();
		
		Assert.that(datasetContext.numMaxSlots() == 1).orThrowMessage("TODO: get mapping slot to agent (projects in dataset have more than 1 slot)");

		var result = new HashMap<Project, Collection<FormedGroup>>();

		agentToProjectMatching.groupedByProject().forEach((proj, agentList) -> {
			Agents agentsWithProject = Agents.from(agentList);
			var groups = new TrivialGroupPartitioning(agentsWithProject);
			result.put(proj, groups.asCollection());
		});

		List<GroupToProjectMatch<Group.FormedGroup>> matchingsAsList = result.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.map(formedGroup -> new GroupToProjectMatch<>(formedGroup, entry.getKey()))
			)
			.collect(Collectors.toList());


		return new FormedGroupToProjectMatching(datasetContext, matchingsAsList);
	}
}
