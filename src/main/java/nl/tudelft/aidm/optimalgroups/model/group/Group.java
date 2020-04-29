package nl.tudelft.aidm.optimalgroups.model.group;

import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreferenceOfAgents;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

public interface Group
{
	Agents members();
	ProjectPreference projectPreference();

	abstract class AbstractGroup implements Group
	{
		protected Agents members;
		protected ProjectPreference projectPreference;

		AbstractGroup(Agents members, ProjectPreference projectPreference)
		{
			this.members = members;
			this.projectPreference = projectPreference;
		}

		public Agents members()
		{
			return members;
		}

		public ProjectPreference projectPreference()
		{
			return projectPreference;
		}
	}

	class FormedGroup extends AbstractGroup
	{
		private final int id;

		public FormedGroup(Agents members, ProjectPreference projectPreference, int id)
		{
			super(members, projectPreference);
			this.id = id;
		}

		public int groupId()
		{
			return id;
		}
	}

	class TentativeGroup extends AbstractGroup
	{
		private int groupMatchScore;

		public TentativeGroup(Agents members, ProjectPreference projectPreference)
		{
			super(members, projectPreference);
		}

		public TentativeGroup(Group group)
		{
			super(group.members(), group.projectPreference());
		}

		public TentativeGroup combined(TentativeGroup other)
		{
			Agents agents = members.with(other.members);
			ProjectPreferenceOfAgents pref = ProjectPreferenceOfAgents.aggregateWithGloballyConfiguredAggregationMethod(agents);

			return new TentativeGroup(agents, pref);
		}
	}
}
