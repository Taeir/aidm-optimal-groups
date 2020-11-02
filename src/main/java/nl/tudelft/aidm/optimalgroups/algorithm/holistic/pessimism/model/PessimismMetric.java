package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism.model;

import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCR;
import nl.tudelft.aidm.optimalgroups.metric.matching.aupcr.AUPCRStudent;
import nl.tudelft.aidm.optimalgroups.metric.rank.WorstAssignedRank;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import plouchtch.lang.exception.ImplementMe;

public record PessimismMetric(AUPCR aupcr, WorstAssignedRank worstRank) implements Comparable<PessimismMetric>
{
	public static PessimismMetric from(AgentToProjectMatching matching)
	{
		return new PessimismMetric(new AUPCRStudent(matching), new WorstAssignedRank.ProjectToStudents(matching));
	}

	public static PessimismMetric impossiblyBad()
	{
		return new PessimismMetric(new ZeroAupcr(), new HugeWorstRank());
	}

	@Override
	public String toString()
	{
		return "Metric - worst: " + worstRank.asInt() + ", aupcr: " + aupcr.asDouble();
	}

	@Override
	public int compareTo(PessimismMetric o)
	{
		// Check which solution has minimized the worst rank better
		// Smaller rank is better, we also want to "maximize" the metric
		// and AUPCR is also "higher is better". So inverse the compareTo
		var rankComparison = -(worstRank.compareTo(o.worstRank));

		// If the worst-ranks are tied, use AUPCR as tie breaker
		if (rankComparison == 0) return aupcr.compareTo(o.aupcr);
		else return rankComparison;
	}

	public static class ZeroAupcr extends AUPCR
	{
		@Override
		public void printResult()
		{
			throw new ImplementMe();
		}

		@Override
		protected float totalArea()
		{
			return 1;
		}

		@Override
		protected int aupc()
		{
			return 0;
		}
	}

	public static class HugeWorstRank implements WorstAssignedRank
	{
		@Override
		public Integer asInt()
		{
			return Integer.MAX_VALUE;
		}
	}

}
