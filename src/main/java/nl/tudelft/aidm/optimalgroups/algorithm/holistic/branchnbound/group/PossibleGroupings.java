package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.group;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface PossibleGroupings
{
	Stream<List<Agent>> of(Set<Agent> include, Set<Agent> possibleGroupmates, GroupSizeConstraint groupSizeConstraint);
}
