package nl.tudelft.aidm.optimalgroups.model.matchfix;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import plouchtch.assertion.Assert;

import java.util.List;
import java.util.stream.Collectors;

public class MatchFixes
{
	private final DatasetContext datasetContext;
	private List<MatchFix> asList;
	
	public MatchFixes(DatasetContext datasetContext, MatchFix... matchFixes)
	{
		this(datasetContext, List.of(matchFixes));
	}
	public MatchFixes(DatasetContext datasetContext, List<MatchFix> matchFixes)
	{
		this.datasetContext = datasetContext;
		asList = matchFixes;
	}
	
	public List<MatchFix> asList()
	{
		return asList;
	}
}
