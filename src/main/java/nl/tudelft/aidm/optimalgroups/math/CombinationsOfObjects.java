package nl.tudelft.aidm.optimalgroups.math;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CombinationsOfObjects<T>
{
	private final List<T> indexedObjects;
	private final int take;

	private final Combinations combinations;

	public
	CombinationsOfObjects(Collection<T> objects, int take)
	{
		this.indexedObjects = new ArrayList<>(objects);
		this.take = take;

		this.combinations = new Combinations(take, indexedObjects.size());
	}

	public
	long count()
	{
		return combinations.count();
	}

	public
	Iterator<List<T>> asIterator()
	{
		var iterCombRaw = combinations.asIterator();

		return new Iterator<>()
		{
			@Override
			public boolean hasNext()
			{
				return iterCombRaw.hasNext();
			}

			@Override
			public List<T> next()
			{
				var rawResult = iterCombRaw.next();

				var listResult = new ArrayList<T>(take);

				for (int i : rawResult) {
					var obj = indexedObjects.get(i);
					listResult.add(obj);
				}

				return listResult;
			}
		};
	}

	public
	Stream<List<T>> asStream()
	{
		var iter = this.asIterator();
		long count = this.count();
		var spliterator = Spliterators.spliterator(iter, count, Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SIZED);

		return StreamSupport.stream(spliterator, false);
	}
}
