package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChiarandiniAgentToProjectMatching
{
	private final List<Match<Agent, Project>> asListOg;
	private final List<Match<Agent, Project>> asListSeq;
	
	private final SequentualDatasetContext seqDatasetContext;
	
	public ChiarandiniAgentToProjectMatching(XVars xVars, SequentualDatasetContext seqDatasetContext)
	{
		var matchesOg = new ArrayList<Match<Agent, Project>>();
		var matchesSeq = new ArrayList<Match<Agent, Project>>();

		seqDatasetContext.allAgents().forEach(agent ->
		{
			seqDatasetContext.allProjects().forEach(project ->
			{
				project.slots().forEach(slot ->
				{
					xVars.of(agent, slot).ifPresent(x ->
					{
						var xValue = x.getValueOrThrow();

						if (xValue > 0.9)
						{
							var matchSeq = new AgentToProjectMatch(agent, project);
							matchesSeq.add(matchSeq);
							
							var ogAgent = seqDatasetContext.mapToOriginal(agent);
							var ogProject = seqDatasetContext.mapToOriginal(project);
							
							var matchOg = new AgentToProjectMatch(ogAgent, ogProject);
							matchesOg.add(matchOg);
						}
					});
				});
			});
		});

		this.asListOg = Collections.unmodifiableList(matchesOg);
		this.asListSeq = Collections.unmodifiableList(matchesSeq);
		
		this.seqDatasetContext = seqDatasetContext;
	}
	
	public AgentToProjectMatching original()
	{
		return new AgentToProjectMatching.Simple(seqDatasetContext.originalContext(), asListOg);
	}
	
	public AgentToProjectMatching sequential()
	{
		return new AgentToProjectMatching.Simple(seqDatasetContext, asListSeq);
	}
}
