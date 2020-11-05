package nl.tudelft.aidm.optimalgroups.math;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class CombinationsTest
{
	@Test
	void worksForEqual()
	{
		Combinations henk = new Combinations(2, 2);
		Iterator<int[]> bla = henk.asIterator();

		while (bla.hasNext()) {
			bla.next();
		}
	}

	@Test
	void worksForn5m3()
	{
		Combinations henk = new Combinations(3, 5);
		Iterator<int[]> bla = henk.asIterator();

		while (bla.hasNext()) {
			bla.next();
		}
	}

	@Test
	void take10From50()
	{
		Combinations henk = new Combinations(10, 50);
		Iterator<int[]> bla = henk.asIterator();

		while (bla.hasNext()) {
			bla.next();
		}
	}
}