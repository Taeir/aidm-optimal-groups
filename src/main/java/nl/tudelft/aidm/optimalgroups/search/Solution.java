package nl.tudelft.aidm.optimalgroups.search;

public interface Solution<METRIC extends Comparable<METRIC>>
{
	METRIC metric();
}
