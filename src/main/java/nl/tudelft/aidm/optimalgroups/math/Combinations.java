package nl.tudelft.aidm.optimalgroups.math;

import plouchtch.assertion.Assert;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class Combinations
{
	private final int take, n;

	public
	Combinations(int take, int n)
	{
		Assert.that(take <= n).orThrowMessage(String.format("take must be <= n (was, take: %s, n: %s)", take, n));

		this.take = take;
		this.n = n;
	}

	public
	long count()
	{
		return numCombinations(n, take);
	}

	public
	Iterator<int[]> asIterator()
	{
		return new CombIterator(take, n);
	}

	private static
	class CombIterator implements Iterator<int[]>
	{
		private final int take, n;

		private final long numResults;
		private long resultsReturned = 0;

		private final int[] result;
		private final Stack<Integer> stack;

		public CombIterator(int take, int n)
		{
			this.take = take;
			this.n = n;

			result = new int[take];

			stack = new Stack<>();

			numResults = numCombinations(n, take);

			if (take > 0)
				stack.push(0);
		}

		@Override
		public boolean hasNext()
		{
			return resultsReturned < numResults;
		}

		@Override
		public int[] next()
		{
			if (take == 0) {
				resultsReturned++;
				return new int[0];
			}

			// Iterative Combinatiorial algorithm
			// Taken from / based on https://rosettacode.org/wiki/Combinations#C.23
			while (!stack.empty())
			{
				int index = stack.size() - 1;
				int value = stack.pop();

				while (value < n)
				{
					result[index++] = value++;
					stack.push(value);

					if (index == take)
					{
						int[] copy = Arrays.copyOf(result, result.length);
						resultsReturned++;
						return copy;
					}
				}
			}

			throw new NoSuchElementException();
		}
	}


	private static
	long numCombinations(int n, int take)
	{
		if (n == 0) return 0;
		if (n == take || take == 0) return 1;

		return fac(n) / ( fac(take) * fac(n - take) );
	}

	private static
	long fac(int x)
	{
		long fac = 1;
		for(int i = 1; i <= x; ++i) fac *= i;

		return fac;
	}
}
