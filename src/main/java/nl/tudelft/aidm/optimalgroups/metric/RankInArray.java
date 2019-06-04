package nl.tudelft.aidm.optimalgroups.metric;

class RankInArray
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
}
