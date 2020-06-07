package nl.tudelft.aidm.optimalgroups.metric.matching.group;

import nl.tudelft.aidm.optimalgroups.metric.NumMatchingArrayElements;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import org.apache.commons.lang3.ArrayUtils;

public class PeerPreferenceSatisfaction
{
	private Group.FormedGroup group;
	private Agent student;

	public PeerPreferenceSatisfaction(Group.FormedGroup group, Agent student)
	{
		this.group = group;
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
		Integer[] groupMembers = group.members().asCollection().stream().map(agent -> agent.id).toArray(Integer[]::new);
		Integer[] pref = ArrayUtils.toObject(student.groupPreference.asArray());

		NumMatchingArrayElements numMatchingArrayElements = new NumMatchingArrayElements(groupMembers, pref);
		int count = numMatchingArrayElements.asInt();

		return count;
	}

	private int peersGiven() {
		return this.student.groupPreference.asArray().length;
	}
}
