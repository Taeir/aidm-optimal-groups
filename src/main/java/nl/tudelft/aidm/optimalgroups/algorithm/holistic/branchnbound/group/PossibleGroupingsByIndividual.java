package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.group;

import nl.tudelft.aidm.optimalgroups.math.CombinationsOfObjects;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PossibleGroupingsByIndividual implements PossibleGroupings
{
	public PossibleGroupingsByIndividual()
	{
	}

	@Override
	public Stream<List<Agent>> of(Set<Agent> include, Set<Agent> possibleGroupmates, GroupSizeConstraint groupSizeConstraint)
	{
		if (include.size() >= groupSizeConstraint.maxSize()) {
			var groups = IntStream.rangeClosed(groupSizeConstraint.minSize(), groupSizeConstraint.maxSize())
				.boxed()
				.sorted(Comparator.reverseOrder())
				.flatMap(grpSize -> {
					var comb = new CombinationsOfObjects<>(include, grpSize);
					return comb.asStream();
				});

			return groups;
		}

		// Must add at least this many agents to "include" to reach the minSize constraint
		int agentsToAddLowerbound = Math.max(groupSizeConstraint.minSize() - include.size(), 0);

		// Cannot add more than this many agents to "include"
		int agentsToAddUpperbound = Math.min(possibleGroupmates.size(), Math.max(groupSizeConstraint.maxSize() - include.size(), 0));

		return IntStream.rangeClosed(agentsToAddLowerbound, agentsToAddUpperbound)
//			.sorted()
			.boxed()
			.flatMap(take -> {
				var comb = new CombinationsOfObjects<>(possibleGroupmates, take);

				return comb.asStream()
					.map(groupmatesToAdd -> {
						var group = new ArrayList<>(include);
						group.addAll(groupmatesToAdd);
						return group;
					});
			});
	}

//	private Result possibleGroups(ProblemInstance problemDef)
//	{
//		Result result = cached.get(problemDef);
//		if (result != null) {
//			return result;
//		}
//
//		var include = problemDef.include;
//		var possibleGroupmates = problemDef.possibleGroupmates;
//		var groupSizeConstraint = problemDef.groupSizeConstraint;
//
//		// TODO OPT: always select agents that are also eccentric with same k
//		if (include.size() == groupSizeConstraint.maxSize()) {
//			return Result.withSingleGroup(include);
//		}
//
//		// When the "must be included" set is larger than a full group, must pick a subset
//		if (include.size() > groupSizeConstraint.maxSize()) {
//			// Generate all possible subsets (that are also valid groups) of the "include" set
//			// Could we be smarter here than letting the matching algo branch-and-bound over all these groups?
//			ProblemInstance def = new ProblemInstance(Set.of(), new LinkedHashSet<>(include), groupSizeConstraint);
//			return possibleGroups(def);
//		}
//
//		if (possibleGroupmates.isEmpty()) {
//			if (include.size() >= groupSizeConstraint.minSize()) {
//				return Result.withSingleGroup(include);
//			}
//
//			return Result.empty;
////				throw new RuntimeException("Can't form group - not enough people available");
//		}
//
//		// Just want to pop an element and add to another set without modifying those
//		// passed in through the arguments
//		var possibleGroupmatesWithout = new LinkedHashSet<>(possibleGroupmates);
//		var iter = possibleGroupmatesWithout.iterator();
//
//		Agent possibleGroupmate = iter.next();
//		iter.remove();
//
//		// include
//		var includeWith = new LinkedHashSet<>(include);
//		includeWith.add(possibleGroupmate);
//		var resultAfterInclude = possibleGroups(new ProblemInstance(includeWith, possibleGroupmatesWithout, groupSizeConstraint));
//
//		// don't include
//		var resultAfterPass = possibleGroups(new ProblemInstance(include, possibleGroupmatesWithout, groupSizeConstraint));
//
//		var combined = new HashSet<>(resultAfterInclude.possibleGroups.size() + resultAfterPass.possibleGroups.size());
//		combined.addAll(resultAfterInclude.possibleGroups);
//		combined.addAll(resultAfterPass.possibleGroups);
//
//		return new Result(combined);
//	}
}
