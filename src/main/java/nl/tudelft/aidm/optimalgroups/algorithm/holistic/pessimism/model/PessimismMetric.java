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

	public static PessimismMetric boundedRank(int rank)
	{
		return new PessimismMetric(new ZeroAupcr(), () -> rank);
	}

	@Override
	public String toString()
	{
		return "Metric - worst: " + worstRank.asInt() + ", aupcr: " + aupcr.asDouble();
	}

	@Override
	public int compareTo(PessimismMetric other)
	{
		// A metric is larger than another if it is better

		// A lower rank is better (larger, positive), so inverse the comparison result
		var rankComparison = -(this.worstRank.compareTo(other.worstRank));

		// If the worst-ranks are tied, use AUPCR as tie breaker
		if (rankComparison != 0)
			return rankComparison;

		// Larger AUPCR values are better
		return this.aupcr.compareTo(other.aupcr);
	}

	public boolean betterThan(PessimismMetric other)
	{
		return this.compareTo(other) > 0;
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
