package nl.tudelft.aidm.optimalgroups.metric.rank;

import java.util.OptionalInt;

public class RankInArray
{
	public <T> OptionalInt determineRank(T value, T[] array)
	{
		int rankNumber = array.length;
		
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(value)) {
				rankNumber = i + 1;
				return OptionalInt.of(rankNumber);
			}
		}

		return OptionalInt.empty();
	}

//	public <T> OptionalInt determineRank(T value, T[] array)
//	{
//		int rankNumber = array.length;
//		for (int i = 0; i < array.length; i++) {
//			if (array[i] == value) {
//				rankNumber = i + 1;
//				return OptionalInt.of(rankNumber);
//			}
//		}
//
//		return OptionalInt.empty();
//	}

}
