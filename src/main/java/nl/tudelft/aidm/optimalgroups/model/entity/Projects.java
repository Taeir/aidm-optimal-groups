package nl.tudelft.aidm.optimalgroups.model.entity;

import java.util.List;

public class Projects
{
	private List<Project> projectList;

	public int count()
	{
		return projectList.size();
	}

	public Project withIndex(int idx)
	{
		return projectList.get(idx);
	}

	private int numTotalSlots;
	public int numTotalSlots()
	{
		// lazy eval
		if (numTotalSlots < 0)
		{
			numTotalSlots = projectList.stream()
				.map(project -> project.numSlots)
				.mapToInt(Integer::intValue)
				.sum();
		}

		return numTotalSlots;
	}
}
