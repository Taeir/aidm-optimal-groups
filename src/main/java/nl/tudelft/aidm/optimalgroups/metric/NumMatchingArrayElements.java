package nl.tudelft.aidm.optimalgroups.metric;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
