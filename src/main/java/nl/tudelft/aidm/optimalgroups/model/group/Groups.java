package nl.tudelft.aidm.optimalgroups.model.group;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public interface Groups<G extends Group>
{
	Collection<G> asCollection();

	void forEach(Consumer<G> fn);

	int count();

	default Agents asAgents()
	{
//		var context = asCollection().stream().flatMap(g -> g.members().asCollection()).

		var agents = this.asCollection().stream()
			.flatMap(g -> g.members().asCollection().stream())
			.collect(Collectors.collectingAndThen(Collectors.toList(), Agents::from));

		return agents;
	}
	
	/**
	 * Filter groups to those only of the given size
	 * @param size Groups must contain this many students
	 * @return Groups of given size
	 */
	default Groups<G> ofSize(int size)
	{
		return this.asCollection().stream()
			        .filter(tentativeGroup -> tentativeGroup.members().count() == size)
			        .collect(collectingAndThen(toList(), Groups.ListBackedImpl<G>::new));
	}
	
	/**
	 * Filter groups to those only of the given sizes
	 * @param sizes The acceptible sizes of groups
	 * @return Groups of given sizes
	 */
	default Groups<G> ofSizes(Integer... sizes)
	{
		var sizesDedup = new HashSet<>(List.of(sizes));
		
		var list = new ArrayList<G>();
		for (int size : sizesDedup)
		{
			list.addAll(this.ofSize(size).asCollection());
		}
		
		return new Groups.ListBackedImpl<G>(list);
	}
	
	default boolean contains(Agent agent)
	{
		return this.asAgents().contains(agent);
	}
	
	/**
	 * Used to check if a group with the given members exists. Or more formally,
	 * given a grouping g' if there is a g in this groups such that g' subset of g
	 * @param subset
	 * @return True if there is a group that is a superset of the given set
	 */
	default boolean containsSupersetOf(G subset)
	{
		var subsetMembers = subset.members().asCollection();
		
		Predicate<G> isSuperset = (G set) -> set.members().asCollection().containsAll(subsetMembers);
		
		Assert.that(this.asCollection().stream().filter(isSuperset).count() <= 1)
			.orThrowMessage("Bugcheck: containsSupersetOf found multiple groups...");
		
		return this.asCollection().stream()
			       .anyMatch(isSuperset);
	}
	
	// =================================================
	
	/**
	 * @return a Groups collection with the given group
	 */
	static <G extends Group> Groups<G> of(G group)
	{
		return Groups.of(List.of(group));
	}
	
	/**
	 * @return a Groups collection with the given groups
	 */
	static <G extends Group> Groups<G> of(List<G> groups)
	{
		return new Groups.ListBackedImpl<>(groups);
	}
	
	// ==================================================
	
	/**
	 * A Groups collection impl backed by a list
	 * @param <G>
	 */
	class ListBackedImpl<G extends Group> extends ListBacked<G>
	{
		private List<G> asList;

		public ListBackedImpl(List<G> asList)
		{
			this.asList = asList;
		}

		@Override
		protected List<G> asList()
		{
			return asList;
		}
	}
	
	/**
	 * An abstract Groups collection impl backed by a list
	 * @param <G> The Group type
	 */
	abstract class ListBacked<G extends Group> implements Groups<G>
	{
		abstract protected List<G> asList();
		
		@Override
		public Collection<G> asCollection()
		{
			return Collections.unmodifiableCollection(this.asList());
		}
		
		@Override
		public void forEach(Consumer<G> fn)
		{
			this.asList().forEach(fn);
		}
		
		@Override
		public int count()
		{
			return this.asList().size();
		}
	}
}
