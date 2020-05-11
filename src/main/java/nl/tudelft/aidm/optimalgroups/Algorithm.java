package nl.tudelft.aidm.optimalgroups;

public interface Algorithm
{
	String name();

	interface Result<A extends Algorithm, R> {
		Algorithm algo();
		R result();
	}
}
