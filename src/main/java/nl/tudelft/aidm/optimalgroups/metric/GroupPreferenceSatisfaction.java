package nl.tudelft.aidm.optimalgroups.metric;

import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.Project;

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
