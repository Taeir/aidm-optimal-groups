package nl.tudelft.aidm.optimalgroups.model.match;

import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectSlotMatching;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// The collection of match tuples
public class FormedGroupToProjectSlotMatching extends ListBasedMatching<Group.FormedGroup, Project.ProjectSlot> implements GroupProjectSlotMatching<Group.FormedGroup>
{
	private Set<Project.ProjectSlot> assignedSlots;
	private Set<Agent> assignedStudents;

	public FormedGroupToProjectSlotMatching()
	{
		super();
		assignedSlots = new HashSet<>();
		assignedStudents = new HashSet<>();
	}

	public void add(Group.FormedGroup group, Project.ProjectSlot slot)
	{
		add(new FormedGroupToProjectSlotMatch(group, slot));
	}

	@Override
	public void add(Match<Group.FormedGroup, Project.ProjectSlot> match)
	{
		/* Sanity checks */

		// Project slot already assigned before?
		if (assignedSlots.contains(match.to())) {
			String msg = String.format("Invalid matchings, project slot already assigned (group: %s, to slot: %s", match.from(), match.to());
			throw new RuntimeException(msg);
		}

		assignedSlots.add(match.to());

		// Any of the studens already assigned before?
		match.from().members().forEach(student -> {
			if (assignedStudents.contains(student)) {
				String msg = String.format("Invalid matchings, student has been already matched (in other group?) (student: %s, group: %s, to slot: %s", student, match.from(), match.to());
				throw new RuntimeException(msg);
			}

			assignedStudents.add(student);
		});

		// all ok!
		super.add(match);
	}

	private WeakReference<FormedGroupToProjectMatching> toProjectMatchingsResult;

	public FormedGroupToProjectMatching toProjectMatchings()
	{
		if (toProjectMatchingsResult != null && toProjectMatchingsResult.get() != null)
			return toProjectMatchingsResult.get();

		var result = this.asList().stream().map(formedGroupProjectSlotMatch -> {
			Group.FormedGroup group = formedGroupProjectSlotMatch.from();
			Project project = formedGroupProjectSlotMatch.to().belongingToProject();

			return new GroupToProjectMatch<>(group, project);
		}).collect(Collectors.toList());

		FormedGroupToProjectMatching formedGroupToProjectMatchings = new FormedGroupToProjectMatching(result);

		toProjectMatchingsResult = new WeakReference<>(formedGroupToProjectMatchings);
		return formedGroupToProjectMatchings;
	}
}
