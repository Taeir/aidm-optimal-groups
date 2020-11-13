package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.List;

public class EmptyMatching extends PessimismMatching
{
	public EmptyMatching(DatasetContext datasetContext)
	{
		super(datasetContext, List.of());
	}

	@Override
	public List<Match<Agent, Project>> asList()
	{
		return List.of();
	}
}
