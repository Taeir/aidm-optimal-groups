package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.algorithm.project.Matching;
import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project.ProjectSlot;

public class GroupPreferenceSatisfaction
{
	private Matching.Match<Group.FormedGroup, ProjectSlot> match;
	private Agent student;

	public GroupPreferenceSatisfaction(Matching.Match<Group.FormedGroup, ProjectSlot> match, Agent student)
	{
		this.match = match;
		this.student = student;
	}

	public String asFraction()
	{
		return String.format("%d/%d", this.peersInGroup(), this.peersGiven());
	}

	public float asFloat() {
		return (this.peersGiven() == 0) ? 1 : ((float) this.peersInGroup()) / ((float) this.peersGiven());
	}

	private int peersInGroup() {
		int[] groupMembers = match.from().members().asCollection().stream().mapToInt(agent -> Integer.decode(agent.name)).toArray();
		int[] pref = student.groupPreference.asArray();

		NumMatchingArrayElements numMatchingArrayElements = new NumMatchingArrayElements(groupMembers, pref);
		int count = numMatchingArrayElements.asInt();

		return count;
	}

	private int peersGiven() {
		return this.student.groupPreference.asArray().length;
	}
}
