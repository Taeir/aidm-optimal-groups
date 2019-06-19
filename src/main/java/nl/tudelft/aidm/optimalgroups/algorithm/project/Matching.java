package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

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
			this(new ArrayList<>());
		}

		ListBasedMatching(List<Match<F,T>> backingList)
		{
			this.backingList = backingList;
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

	class FormedGroupToProjecMatchings extends ListBasedMatching<Group.FormedGroup, Project> implements GroupProjectMatching<Group.FormedGroup>
	{
		FormedGroupToProjecMatchings(List<? extends Match<Group.FormedGroup, Project>> list)
		{
			super((List<Match<Group.FormedGroup, Project>>) list);
		}
	}

	// The collection of match tuples
	class FormedGroupToProjectSlotMatchings extends ListBasedMatching<Group.FormedGroup, Project.ProjectSlot> implements GroupProjectSlotMatching<Group.FormedGroup>
	{
		private Set<Project.ProjectSlot> assignedSlots;
		private Set<Agent> assignedStudents;

		public FormedGroupToProjectSlotMatchings()
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

		private WeakReference<FormedGroupToProjecMatchings> toProjectMatchingsResult;

		public FormedGroupToProjecMatchings toProjectMatchings()
		{
			if (toProjectMatchingsResult != null && toProjectMatchingsResult.get() != null)
				return toProjectMatchingsResult.get();

			var result = this.asList().stream().map(formedGroupProjectSlotMatch -> {
				Group.FormedGroup group = formedGroupProjectSlotMatch.from();
				Project project = formedGroupProjectSlotMatch.to().belongingToProject();

				return new GroupToProjectMatch<>(group, project);
			}).collect(Collectors.toList());

			FormedGroupToProjecMatchings formedGroupToProjecMatchings = new FormedGroupToProjecMatchings(result);

			toProjectMatchingsResult = new WeakReference<>(formedGroupToProjecMatchings);
			return formedGroupToProjecMatchings;
		}
	}

	// The match tuple
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

	class AgentToProjectMatch implements Match<Agent, Project>
	{
		private final Agent agent;
		private final Project project;

		public AgentToProjectMatch(Agent agent, Project project)
		{
			this.agent = agent;
			this.project = project;
		}

		@Override
		public Agent from()
		{
			return agent;
		}

		@Override
		public Project to()
		{
			return project;
		}
	}

	class GroupToProjectMatch<G extends Group> implements Match<G, Project>
	{
		private final G group;
		private final Project project;

		public GroupToProjectMatch(G group, Project project)
		{
			this.group = group;
			this.project = project;
		}

		@Override
		public G from()
		{
			return group;
		}

		@Override
		public Project to()
		{
			return project;
		}
	}
}
