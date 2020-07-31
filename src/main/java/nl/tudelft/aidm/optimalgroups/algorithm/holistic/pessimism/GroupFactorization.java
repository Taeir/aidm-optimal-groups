package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;

import java.util.Arrays;

public class GroupFactorization
{
	private final GroupSizeConstraint groupSizeConstraint;
	private final Boolean[] isFactorable;

	public GroupFactorization(GroupSizeConstraint groupSizeConstraint, int numStudentsMax)
	{
		this.groupSizeConstraint = groupSizeConstraint;
		this.isFactorable = new Boolean[numStudentsMax];
		Arrays.fill(isFactorable, null);
	}

	public boolean isFactorableIntoValidGroups(int numStudents)
	{
		if (isFactorable[numStudents] == null) {
			
		}
	}
}
