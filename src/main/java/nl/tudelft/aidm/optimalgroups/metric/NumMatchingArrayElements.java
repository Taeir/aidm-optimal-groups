package nl.tudelft.aidm.optimalgroups.metric;

public class NumMatchingArrayElements
{
	private final int[] array;
	private final int[] elementsToFind;

	public NumMatchingArrayElements(int[] array, int[] elementsToFind)
	{
		this.array = array;
		this.elementsToFind = elementsToFind;
	}

	public int asInt()
	{
		int numFound = 0;
		for (int arrayElem : array)
		{
			for (int elemToFind : elementsToFind)
			{
				if (arrayElem == elemToFind) {
					numFound++;
				}
			}
		}

		return numFound;
	}
}
