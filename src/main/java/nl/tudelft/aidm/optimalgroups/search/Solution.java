package nl.tudelft.aidm.optimalgroups.search;

public interface Solution<METRIC>
{
	METRIC metric();

	boolean isBetterThan(Solution other);
}
