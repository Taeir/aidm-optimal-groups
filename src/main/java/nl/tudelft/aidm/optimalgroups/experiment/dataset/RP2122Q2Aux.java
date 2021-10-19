package nl.tudelft.aidm.optimalgroups.experiment.dataset;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.SimpleAgent;
import nl.tudelft.aidm.optimalgroups.model.matchfix.MatchFix;
import nl.tudelft.aidm.optimalgroups.model.matchfix.MatchFixes;
import plouchtch.assertion.Assert;

import java.util.List;

public class RP2122Q2Aux
{
	public static boolean agentsFilter(Agent.AgentInBepSysSchemaDb agent)
	{
		var agentBepsysId = agent.bepSysUserId;
		
		var agentIsInFilterList = agentsToFilterOut().contains(agentBepsysId);
		return !agentIsInFilterList;
	}
	
	public static MatchFixes matchesToFix(ResearchProject2122Q2Dataset dataset)
	{
//		throw new RuntimeException("Fill me in");
		return new MatchFixes(dataset,
		);
	}
	
	public static List<Integer> agentsToFilterOut()
	{
//		throw new RuntimeException("Fill me in");
		return List.of(
		
		);
	}
}
