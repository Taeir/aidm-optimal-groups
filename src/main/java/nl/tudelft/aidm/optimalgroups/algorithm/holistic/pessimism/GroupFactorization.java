package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import plouchtch.assertion.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GroupFactorization
{
	public static record Factorization(boolean isFactorable, int numGroupsOfMinSize, int numGroupsOfMaxSize) {};

	private List<Factorization> isFactorable;
	private final GroupSizeConstraint groupSizeConstraint;


	// TODO: weak references solution if to be used in production environment
	public static Map<GroupSizeConstraint, GroupFactorization> sharedInstances = new HashMap<>();
	public static synchronized GroupFactorization cachedInstanceFor(GroupSizeConstraint groupSizeConstraint)
	{
		return sharedInstances.computeIfAbsent(groupSizeConstraint,
			key -> new GroupFactorization(groupSizeConstraint, 1000)
		);
	}


	/* CTOR */
	public GroupFactorization(GroupSizeConstraint groupSizeConstraint, int expectedStudentsMax)
	{
		this.groupSizeConstraint = groupSizeConstraint;

		this.isFactorable = makeFreshLookupList(expectedStudentsMax);

		Assert.that(groupSizeConstraint.maxSize() - groupSizeConstraint.minSize() == 1)
			.orThrowMessage("Fix group factorization to support delta != 1");
	}


	public synchronized Factorization forGivenNumberOfStudents(int numStudents) {
		isFactorableIntoValidGroups(numStudents);

		return isFactorable.get(numStudents);
	}

	public synchronized boolean isFactorableIntoValidGroups(int numStudents)
	{
		if (numStudents + 1 > isFactorable.size()) {
			// If larger size is requested, expand list
			isFactorable = copyIntoResized(isFactorable, numStudents);
		}

		var resultForNumStudents = isFactorable.get(numStudents);
		if (resultForNumStudents != null) {
			return resultForNumStudents.isFactorable;
		}

		// Not previously evaluted, compute result:

		int maxGroupSize = groupSizeConstraint.maxSize();
		int minGroupSize = groupSizeConstraint.minSize();

		for (int i = 0; i <= numStudents / maxGroupSize; i++) {
			for (int j = 0; j <= (numStudents - i * maxGroupSize) / minGroupSize; j++) {
				int n = maxGroupSize * i + minGroupSize * j;
				if (n == numStudents) {
					var factorization = new Factorization(true, j, i);
					isFactorable.set(numStudents, factorization);
					return true;
				}
			}
		}

		var factorization = new Factorization(false, 0, 0);
		isFactorable.set(numStudents, factorization);
		return false;
	}


	/* HELPER FNS */
	private static List<Factorization> makeFreshLookupList(int upToIndexInclusive)
	{
		return makeLookupList(upToIndexInclusive, index -> null);
	}

	private static List<Factorization> copyIntoResized(List<Factorization> old, int upToIndexInclusive)
	{
		// Function ensures value in copy are same as 'old' and any higher indexed elements (= 'new' elements)
		// are set to 'null' (= unknown yet)
		Function<Integer, Factorization> indexToValueFn = index -> {
			var value = index < old.size()
				? old.get(index)
				: null;

			return value;
		};

		return makeLookupList(upToIndexInclusive, indexToValueFn);
	}

	private static List<Factorization> makeLookupList(int upToIndexInclusive, Function<Integer, Factorization> indexToValueFn)
	{
		var list = new ArrayList<Factorization>(upToIndexInclusive);
		for (int i = 0; i <= upToIndexInclusive; i++)
		{
			list.add(null);
		}

		int upToExclusive = upToIndexInclusive + 1;
		IntStream.range(0, upToExclusive).forEach(index -> list.set(index, indexToValueFn.apply(index)));

		return list;
	}
}
