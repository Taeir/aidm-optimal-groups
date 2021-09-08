package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChiarandiniAgentToProjectMatching implements AgentToProjectMatching
{
	private final List<Match<Agent, Project>> asList;
	private final DatasetContext datasetContext;
	
	public ChiarandiniAgentToProjectMatching(AssignmentConstraints.XVars xVars, DatasetContext datasetContext)
	{
		var matchesOg = new ArrayList<Match<Agent, Project>>();
		var asList = new ArrayList<Match<Agent, Project>>();

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
							var matchSeq = new AgentToProjectMatch(agent, project);
							asList.add(matchSeq);
						}
					});
				});
			});
		});

		this.asList = Collections.unmodifiableList(asList);
		this.datasetContext = datasetContext;
	}
	
	@Override
	public List<Match<Agent, Project>> asList()
	{
		return this.asList;
	}
	
	@Override
	public DatasetContext datasetContext()
	{
		return this.datasetContext;
	}
}
