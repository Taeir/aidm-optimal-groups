package nl.tudelft.aidm.optimalgroups.model.comparison;

import gurobi.GRB;
import gurobi.GRBLinExpr;
import nl.tudelft.aidm.optimalgroups.model.Profile;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class ParetoComperator
{
	public enum ParetoOutcome {
		SAME, BETTER, WORSE, NONE
	}
	
	public ParetoOutcome compare(Profile profile, Profile other)
	{
		if (profile.equals(other)) {
			return ParetoOutcome.SAME;
		}
		
		if (isParetoBetter(profile, other)) {
			return ParetoOutcome.BETTER;
		}
		else if (isParetoBetter(other, profile)) {
			return ParetoOutcome.WORSE;
		}
		else {
			// Note same, not better, not worse - just not pareto
			return ParetoOutcome.NONE;
		}
	}
	
	public boolean isParetoBetter(Profile profile, Profile other)
	{
		var maxRank = Math.max(profile.maxRank(), other.maxRank());
		
		var rankCumSumsProfile = cumSumOf(profile, maxRank);
		var rankCumSumsOther = cumSumOf(other, maxRank);
		
		boolean profileIsBetterOrSame = true;
		
		for (int i = 0; i < maxRank + 1; i++)
		{
			profileIsBetterOrSame &= rankCumSumsProfile[i] >= rankCumSumsOther[i];
		}
		
		return profileIsBetterOrSame;
	}
	
	
	private int[] cumSumOf(Profile profile, int maxRank)
	{
		var cumSum = new int[maxRank+1];
		cumSum[0] = 0; // no such rank / empty
		
		for (int i = 1; i < maxRank; i++)
		{
			cumSum[i] = cumSum[i-1] + profile.numAgentsWithRank(i);
		}
		
		return cumSum;
	}
	
	
}
