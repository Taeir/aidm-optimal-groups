package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism.groups;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;

import java.util.LinkedHashSet;
import java.util.Set;

public interface PossibleGroupings
{
	Set<Set<Agent>> of(Set<Agent> include, LinkedHashSet<Agent> possibleGroupmates, GroupSizeConstraint groupSizeConstraint);
}
