package nl.tudelft.aidm.optimalgroups.math;

import plouchtch.assertion.Assert;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class Combinations
{
	private final int m, n;

	public
	Combinations(int m, int n)
	{
		Assert.that(m <= n).orThrowMessage(String.format("m must be <= n (was, m: %s, n: %s)", m, n));

		this.m = m;
		this.n = n;
	}

	public
	long count()
	{
		return numCombinations(n, m);
	}

	public
	Iterator<int[]> asIterator()
	{
		return new CombIterator(m, n);
	}

	private static
	class CombIterator implements Iterator<int[]>
	{
		private final int m, n;

		private final long numResults;
		private long resultsReturned = 0;

		private final int[] result;
		private final Stack<Integer> stack;

		public CombIterator(int m, int n)
		{
			this.m = m;
			this.n = n;

			result = new int[m];

			stack = new Stack<>();

			numResults = numCombinations(n, m);

			if (m > 0)
				stack.push(0);
		}

		@Override
		public boolean hasNext()
		{
			return resultsReturned < numResults && m > 0;
		}

		@Override
		public int[] next()
		{
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

					if (index == m)
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
		if (n == take) return 1;

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
