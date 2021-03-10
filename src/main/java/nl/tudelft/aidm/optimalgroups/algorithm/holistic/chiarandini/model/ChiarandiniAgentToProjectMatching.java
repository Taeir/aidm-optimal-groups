package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.XVars;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
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

	public ChiarandiniAgentToProjectMatching(XVars xVars, SequentualDatasetContext datasetContext)
	{
		var matches = new ArrayList<Match<Agent, Project>>();

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
							var ogAgent = datasetContext.mapToOriginal(agent);
							var ogProject = datasetContext.mapToOriginal(project);
							var match = new AgentToProjectMatch(ogAgent, ogProject);
							matches.add(match);
						}
					});
				});
			});
		});

		this.asList = Collections.unmodifiableList(matches);
		this.datasetContext = datasetContext.originalContext();
	}

	@Override
	public List<Match<Agent, Project>> asList()
	{
		return this.asList;
	}

	@Override
	public DatasetContext datasetContext()
	{
		return datasetContext;
	}
}
