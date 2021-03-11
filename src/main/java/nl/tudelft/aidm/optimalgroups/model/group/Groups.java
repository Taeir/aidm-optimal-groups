package nl.tudelft.aidm.optimalgroups.model.group;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
}
