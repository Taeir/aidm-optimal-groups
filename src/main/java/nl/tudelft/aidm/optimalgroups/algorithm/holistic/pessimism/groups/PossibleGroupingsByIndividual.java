package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism.groups;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;

import java.util.*;

public class PossibleGroupingsByIndividual implements PossibleGroupings
{
	private record ProblemInstance(Set<Agent> include, LinkedHashSet<Agent> possibleGroupmates, GroupSizeConstraint groupSizeConstraint){}
	private record Result(Set<Set<Agent>> possibleGroups)
	{
		public static final Result empty = new Result(Set.of());
		public static Result withSingleGroup(Set<Agent> group)
		{
			return new Result(Set.of(new HashSet<>(group)));
		}
	}

	private HashMap<ProblemInstance, Result> cached;

	public PossibleGroupingsByIndividual()
	{
		cached = new HashMap<>();
	}

	@Override
	public Set<Set<Agent>> of(Set<Agent> include, LinkedHashSet<Agent> possibleGroupmates, GroupSizeConstraint groupSizeConstraint)
	{
		var problemDef = new ProblemInstance(include, possibleGroupmates, groupSizeConstraint);
		var result = cached.get(problemDef);

		if (result == null) {
			// Mandatory include can form a group already - optimal case, don't even try generating powersets
//			if (include.size() >= groupSizeConstraint.minSize() && include.size() <= groupSizeConstraint.maxSize()) {
//				result = new Result(Set.of(include));
//			}
//			else {
				result = possibleGroups(problemDef);
//			}
			cached.put(problemDef, result);
		}
		else {
			return result.possibleGroups;
		}

		return result.possibleGroups();
	}

	private Result possibleGroups(ProblemInstance problemDef)
	{
		Result result = cached.get(problemDef);
		if (result != null) {
			return result;
		}

		var include = problemDef.include;
		var possibleGroupmates = problemDef.possibleGroupmates;
		var groupSizeConstraint = problemDef.groupSizeConstraint;

		// TODO OPT: always select agents that are also eccentric with same k
		if (include.size() == groupSizeConstraint.maxSize()) {
			return Result.withSingleGroup(include);
		}

		// When the "must be included" set is larger than a full group, must pick a subset
		if (include.size() > groupSizeConstraint.maxSize()) {
			// Generate all possible subsets (that are also valid groups) of the "include" set
			// Could we be smarter here than letting the matching algo branch-and-bound over all these groups?
			ProblemInstance def = new ProblemInstance(Set.of(), new LinkedHashSet<>(include), groupSizeConstraint);
			return possibleGroups(def);
		}

		if (possibleGroupmates.isEmpty()) {
			if (include.size() >= groupSizeConstraint.minSize()) {
				return Result.withSingleGroup(include);
			}

			return Result.empty;
//				throw new RuntimeException("Can't form group - not enough people available");
		}

		// Just want to pop an element and add to another set without modifying those
		// passed in through the arguments
		var possibleGroupmatesWithout = new LinkedHashSet<>(possibleGroupmates);
		var iter = possibleGroupmatesWithout.iterator();

		Agent possibleGroupmate = iter.next();
		iter.remove();

		// include
		var includeWith = new HashSet<>(include);
		includeWith.add(possibleGroupmate);
		var resultAfterInclude = possibleGroups(new ProblemInstance(includeWith, possibleGroupmatesWithout, groupSizeConstraint));

		// don't include
		var resultAfterPass = possibleGroups(new ProblemInstance(include, possibleGroupmatesWithout, groupSizeConstraint));

		var combined = new HashSet<>(resultAfterInclude.possibleGroups);
		combined.addAll(resultAfterPass.possibleGroups);

		return new Result(combined);
	}
}
