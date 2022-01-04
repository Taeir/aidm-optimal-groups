package nl.tudelft.aidm.optimalgroups.experiment.dataset;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matchfix.MatchFix;
import nl.tudelft.aidm.optimalgroups.model.matchfix.MatchFixes;

import java.util.List;

public class RP2021Q4Aux
{
	public static boolean agentsFilter(Agent.AgentInBepSysSchemaDb agent)
	{
		var agentBepsysId = agent.bepSysUserId;
		
		var agentIsInFilterList = agentsToFilterOut().contains(agentBepsysId);
		return !agentIsInFilterList;
	}
	
	public static MatchFixes matchesToFix(ResearchProject2021Q4Dataset dataset)
	{
		// No fixes
//		return new MatchFixes(dataset);
//		throw new RuntimeException("Fill me in");
		
		return new MatchFixes(dataset
		);
	}
	
	public static List<Integer> agentsToFilterOut()
	{
		// No filters
		return List.of();
//		throw new RuntimeException("Fill me in");
	}
}
