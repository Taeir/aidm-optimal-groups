package nl.tudelft.aidm.optimalgroups.model.group;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public interface Groups<G extends Group>
{
	Collection<G> asCollection();

	void forEach(Consumer<G> fn);

	int count();

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
