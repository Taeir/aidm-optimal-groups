package nl.tudelft.aidm.optimalgroups.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CombinationsOfObjects<T>
{
	private final List<T> indexedObjects;
	private final int take;

	public CombinationsOfObjects(Collection<T> objects, int take)
	{
		this.indexedObjects = new ArrayList<>(objects);
		this.take = take;
	}

	public
	Iterator<List<T>> asIterator()
	{
		var iterCombRaw = new Combinations(take, indexedObjects.size()).asIterator();

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
}
