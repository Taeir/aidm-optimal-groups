package nl.tudelft.aidm.optimalgroups.model.entity;

public class Project
{
	public String name;
	public final int numSlots;

	public Project(String name, int numSlots)
	{
		this.name = name;
		this.numSlots = numSlots;
	}
}
