package nl.tudelft.aidm.optimalgroups.metric;

public class RankInArray
{
	public int determineRank(int value, int[] array)
	{
		int rankNumber = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				rankNumber = i + 1;
				break;
			}
		}

		return rankNumber;
	}

	public int determineRank(int value, Integer[] array)
	{
		int rankNumber = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				rankNumber = i + 1;
				break;
			}
		}

		return rankNumber;
	}

}
