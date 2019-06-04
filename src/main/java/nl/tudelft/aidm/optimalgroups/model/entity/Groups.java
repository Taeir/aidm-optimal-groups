package nl.tudelft.aidm.optimalgroups.model.entity;

import java.util.Collection;
import java.util.function.Consumer;

public interface Groups
{
	Collection<Group> asCollection();
	void forEach(Consumer<Group> fn);
	int count();
}
