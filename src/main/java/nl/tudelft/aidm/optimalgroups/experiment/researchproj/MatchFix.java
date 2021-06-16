package nl.tudelft.aidm.optimalgroups.experiment.researchproj;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.base.ListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record MatchFix(Group group, Project project)
{
//	public static MatchFix fromIds(DatasetContext ogDatasetContext, Integer projectId, Integer... agentIds)
//	{
//		Assert.that(!(ogDatasetContext instanceof SequentualDatasetContext)).orThrowMessage("must be og dataset");
//
//		var proj = ogDatasetContext.allProjects().findWithId(projectId).orElseThrow();
//
//		var agents = Arrays.stream(agentIds).map(id -> ogDatasetContext.allAgents().findByAgentId(id).orElseThrow())
//			.collect(Collectors.collectingAndThen(Collectors.toList(), Agents::from));
//
//		return new MatchFix(agents, proj);
//	}
	
	public static MatchFix fromIdsToSeq(DatasetContext og, SequentualDatasetContext seq, Integer projectId, Integer... agentIds)
	{
		Assert.that(!(og instanceof SequentualDatasetContext)).orThrowMessage("must be og dataset");
		
		var proj = og.allProjects().findWithId(projectId).orElseThrow();
		var projSeq = seq.allProjects().correspondingSequentualProjectOf(proj);
		
		var agentsSeq = Arrays.stream(agentIds)
			             .map(id -> {
			             	var maybe = og.allAgents().findByAgentId(id);
			             	return maybe.or(() -> {
			             		System.out.printf("Warning: match cannot fixed for agent %s - agent is missing\n", id);
			             		return Optional.empty();
			                });
			             })
						 .flatMap(Optional::stream)
						 .map(ogAgent -> (Agent) seq.allAgents().correspondingSeqAgentOf(ogAgent))
			             .collect(Collectors.collectingAndThen(Collectors.toList(), Agents::from));
		
		return new MatchFix(new FixedMatchGroup(agentsSeq, projSeq), projSeq);
	}
	
	private static class FixedMatchGroup implements Group
	{
		private final Agents members;
		private final ProjectPreference singleProjectPref;
		
		public FixedMatchGroup(Agents members, Project fixedToProject)
		{
			this.members = members;
			this.singleProjectPref = new ListBasedProjectPreferences(this, List.of(fixedToProject));
		}
		
		@Override
		public Agents members()
		{
			return members;
		}
		
		@Override
		public ProjectPreference projectPreference()
		{
			return singleProjectPref;
		}
	}
}
