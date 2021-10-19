package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.model;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.ListBasedMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PessimismMatching extends ListBasedMatching<Agent, Project> implements AgentToProjectMatching
{
	public PessimismMatching(DatasetContext datasetContext, List<Match<Agent, Project>> matches)
	{
		super(datasetContext, matches);
	}

	public PessimismMatching(List<Match<Agent, Project>> matches)
	{
		super(
			matches.stream().map(match -> match.from().datasetContext()).findAny().orElseThrow(),
			List.copyOf(matches)
		);
	}

	public PessimismMatching withMatches(Project project, Collection<Agent> agents)
	{
		var matchedWithNew = new ArrayList<Match<Agent, Project>>(this.asList().size() + agents.size());
		matchedWithNew.addAll(this.asList());

		agents.forEach(agent -> {
			var match = new AgentToProjectMatch(agent, project);
			matchedWithNew.add(match);
		});

		return new PessimismMatching(datasetContext(), matchedWithNew);
	}
}
