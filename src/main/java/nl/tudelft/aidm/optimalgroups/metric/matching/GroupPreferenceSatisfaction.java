package nl.tudelft.aidm.optimalgroups.metric.matching;

import nl.tudelft.aidm.optimalgroups.metric.NumMatchingArrayElements;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public class GroupPreferenceSatisfaction
{
	private Match<Group.FormedGroup, Project> match;
	private Agent student;

	public GroupPreferenceSatisfaction(Match<Group.FormedGroup, Project> match, Agent student)
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
		Group.FormedGroup group = match.from();
		int[] groupMembers = group.members().asCollection().stream().mapToInt(agent -> agent.id).toArray();
		int[] pref = student.groupPreference.asArray();

		NumMatchingArrayElements numMatchingArrayElements = new NumMatchingArrayElements(groupMembers, pref);
		int count = numMatchingArrayElements.asInt();

		return count;
	}

	private int peersGiven() {
		return this.student.groupPreference.asArray().length;
	}
}
