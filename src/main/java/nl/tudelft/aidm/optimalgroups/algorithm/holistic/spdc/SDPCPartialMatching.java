package nl.tudelft.aidm.optimalgroups.algorithm.holistic.spdc;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.ListBasedMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.ArrayList;
import java.util.List;

class SDPCPartialMatching extends ListBasedMatching<Agent, Project> implements AgentToProjectMatching
{
	public SDPCPartialMatching(DatasetContext datasetContext)
	{
		super(datasetContext);
	}

	private SDPCPartialMatching(DatasetContext datasetContext, List<Match<Agent, Project>> matches)
	{
		super(datasetContext, matches);
	}

	public SDPCPartialMatching withNewMatch(Agent agent, Project project)
	{
		List<Match<Agent, Project>> matches = new ArrayList<>(this.asList());
		matches.add(new AgentToProjectMatch(agent, project));

		return new SDPCPartialMatching(datasetContext(), matches);
	}
}
