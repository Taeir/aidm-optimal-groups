package nl.tudelft.aidm.optimalgroups.model.matchfix;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
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
	
	public MatchFixes forSequentual(SequentualDatasetContext seqDatasetContext)
	{
		Assert.that(seqDatasetContext.originalContext() == this.datasetContext)
			.orThrowMessage("DatasetContext mismatch (seq not based on the indended database)");
		
		return this.asList().stream()
			.map(matchFix -> {
				// map all projects and agents to their seq. counterparts
				var seqProj = seqDatasetContext.allProjects().correspondingSequentualProjectOf(matchFix.project());
				var seqAgents = matchFix.group().members().asCollection().stream()
					                .map(agent -> (Agent) seqDatasetContext.allAgents().correspondingSeqAgentOf(agent))
					                .collect(Collectors.collectingAndThen(Collectors.toList(), Agents::from));
				
				return MatchFix.from(seqAgents, seqProj);
			})
			.collect(Collectors.collectingAndThen(Collectors.toList(), x -> new MatchFixes(seqDatasetContext, x)));
	}
}
