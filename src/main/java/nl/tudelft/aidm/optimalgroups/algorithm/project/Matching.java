package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		FROM group();
		TO project();
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

	class GroupToProjectMatch implements Match<Group, Project>
	{
		Group group;
		Project project;

		public GroupToProjectMatch(Group group, Project project)
		{
			this.group = group;
			this.project = project;
		}

		@Override
		public Group group()
		{
			return group;
		}

		@Override
		public Project project()
		{
			return project;
		}
	}
}
