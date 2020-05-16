package nl.tudelft.aidm.optimalgroups.metric.rank;

import java.util.OptionalInt;

public class RankInArray
{
	public OptionalInt determineRank(int value, int[] array)
	{
		int rankNumber = array.length;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				rankNumber = i + 1;
				return OptionalInt.of(rankNumber);
			}
		}

		return OptionalInt.empty();
	}

	public OptionalInt determineRank(int value, Integer[] array)
	{
		int rankNumber = array.length;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				rankNumber = i + 1;
				return OptionalInt.of(rankNumber);
			}
		}

		return OptionalInt.empty();
	}

}
