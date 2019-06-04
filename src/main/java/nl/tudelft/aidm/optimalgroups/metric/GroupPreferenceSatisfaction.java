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
		int[] groupMembers = match.from().members().asCollection().stream().mapToInt(agent -> Integer.decode(agent.name)).toArray();
		int[] pref = student.groupPreference.asArray();

		NumMatchingArrayElements numMatchingArrayElements = new NumMatchingArrayElements(groupMembers, pref);
		int count = numMatchingArrayElements.asInt();
		int countPref = pref.length;

		return String.format("%s/%s", count, countPref);
	}
}
