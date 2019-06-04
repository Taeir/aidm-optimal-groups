package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

import java.util.*;

/**
 * The result of a matching algorithm
 */
public interface Matching<FROM, TO>
{
	/**
	 * The matching as a list representation
	 * @return An unmodifiable list
	 */
	List<Match<FROM, TO>> asList();

	/**
	 * The match between a group/agent and project
	 */
	interface Match<FROM, TO>
	{
		FROM from();
		TO to();
	}

	class ListBasedMatching<F, T> implements Matching<F, T>
	{
		private List<Match<F, T>> backingList;

		public ListBasedMatching()
		{
			backingList = new ArrayList<>();
		}

		public void add(Match<F,T> match)
		{
			this.backingList.add(match);
		}

		@Override
		public List<Match<F, T>> asList()
		{
			return Collections.unmodifiableList(backingList);
		}
	}

	class FormedGroupToProjectMatchings extends ListBasedMatching<Group.FormedGroup, Project.ProjectSlot>
	{
		private Set<Project.ProjectSlot> assignedSlots;
		private Set<Agent> assignedStudents;

		public FormedGroupToProjectMatchings()
		{
			super();
			assignedSlots = new HashSet<>();
			assignedStudents = new HashSet<>();
		}

		@Override
		public void add(Match<Group.FormedGroup, Project.ProjectSlot> match)
		{
			/* Sanity checks */

			// Project slot already assigned before?
			if (assignedSlots.contains(match.to())) {
				String msg = String.format("Invalid matching, project slot already assigned (group: %s, to slot: %s", match.from(), match.to());
				throw new RuntimeException(msg);
			}

			assignedSlots.add(match.to());

			// Any of the studens already assigned before?
			match.from().members().forEach(student -> {
				if (assignedStudents.contains(student)) {
					String msg = String.format("Invalid matching, student has been already matched (in other group?) (student: %s, group: %s, to slot: %s", student, match.from(), match.to());
					throw new RuntimeException(msg);
				}

				assignedStudents.add(student);
			});

			// all ok!
			super.add(match);
		}
	}

	class FormedGroupToProjectSlotMatch implements Match<Group.FormedGroup, Project.ProjectSlot>
	{
		Group.FormedGroup group;
		Project.ProjectSlot project;

		public FormedGroupToProjectSlotMatch(Group.FormedGroup group, Project.ProjectSlot slot)
		{
			this.group = group;
			this.project = slot;
		}

		@Override
		public Group.FormedGroup from()
		{
			return group;
		}

		@Override
		public Project.ProjectSlot to()
		{
			return project;
		}

		public Group group()
		{
			return group;
		}

		public Project.ProjectSlot projectSlot()
		{
			return project;
		}
	}
}
