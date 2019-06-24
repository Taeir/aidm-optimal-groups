package nl.tudelft.aidm.optimalgroups.model.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListBasedMatchings<F, T> implements Matchings<F, T>
{
	private List<Match<F, T>> backingList;

	public ListBasedMatchings()
	{
		this(new ArrayList<>());
	}

	ListBasedMatchings(List<Match<F,T>> backingList)
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
