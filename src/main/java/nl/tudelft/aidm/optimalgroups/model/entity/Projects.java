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
}
