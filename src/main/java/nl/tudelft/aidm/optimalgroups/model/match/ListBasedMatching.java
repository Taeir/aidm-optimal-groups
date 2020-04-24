package nl.tudelft.aidm.optimalgroups.model.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListBasedMatching<F, T> implements Matching<F, T>
{
	private List<Match<F, T>> backingList;

	public ListBasedMatching()
	{
		this(new ArrayList<>());
	}

	ListBasedMatching(List<Match<F,T>> backingList)
	{
		this.backingList = backingList;
	}

	public void add(Match<F,T> match)
	{
		this.backingList.add(match);
	}

	@Override
	public List<Match<F, T>> asList()
	{
		return Collections.unmodifiableList(backingList);
	}
}
