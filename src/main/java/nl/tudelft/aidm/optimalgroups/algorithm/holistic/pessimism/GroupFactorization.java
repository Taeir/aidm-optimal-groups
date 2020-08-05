package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import plouchtch.assertion.Assert;

import java.util.Arrays;

public class GroupFactorization
{
	private final GroupSizeConstraint groupSizeConstraint;
	private final Boolean[] isFactorable;
	private final int numStudentsMax;

	public GroupFactorization(GroupSizeConstraint groupSizeConstraint, int numStudentsMax)
	{
		this.groupSizeConstraint = groupSizeConstraint;
		this.numStudentsMax = numStudentsMax;
		this.isFactorable = new Boolean[numStudentsMax + 1];

		Arrays.fill(isFactorable, null);

		Assert.that(groupSizeConstraint.maxSize() - groupSizeConstraint.minSize() == 1)
			.orThrowMessage("Fix group factorization to support delta > 1");
	}

	public boolean isFactorableIntoValidGroups(int numStudents)
	{
		if (isFactorable[numStudents] == null) {
			int a = groupSizeConstraint.maxSize();
			int b = groupSizeConstraint.minSize();

			for (int i = 0; i <= numStudents / a; i++) {
				for (int j = 0; j <= (numStudents - i * a) / b; j++) {
					int n = a * i + b * j;
					if (n == numStudents) {
						isFactorable[numStudents] = true;
						return true;
					}
				}
			}

			isFactorable[numStudents] = false;
		}

		return isFactorable[numStudents];
	}
}
