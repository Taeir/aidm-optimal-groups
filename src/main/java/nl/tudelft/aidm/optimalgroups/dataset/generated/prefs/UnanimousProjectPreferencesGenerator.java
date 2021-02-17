package nl.tudelft.aidm.optimalgroups.dataset.generated.prefs;

import nl.tudelft.aidm.optimalgroups.metric.rank.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankOfCompletelyIndifferentAgent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Identical preference shared by everybody!
 */
public class UnanimousProjectPreferencesGenerator implements PreferenceGenerator
{
	private final ProjectPreference theOne;

	/**
	 * The given preference is the unanimous preference
	 * @param theOne The unanimous preference
	 */
	public UnanimousProjectPreferencesGenerator(ProjectPreference theOne)
	{
		this.theOne = theOne;
	}
	@Override
	public ProjectPreference generateNew(Supplier<Agent> ownerSupplier)
	{
		return theOne;
	}

	private class PreferenceCopy implements ProjectPreference
	{
		private Supplier<Agent> ownerSupplier;

		public PreferenceCopy(Supplier<Agent> ownerSupplier)
		{
			this.ownerSupplier = ownerSupplier;
		}

		@Override
		public Integer[] asArray()
		{
			return theOne.asArray();
		}

		@Override
		public List<Project> asListOfProjects()
		{
			return theOne.asListOfProjects();
		}

		@Override
		public Object owner()
		{
			return ownerSupplier.get();
		}

		@Override
		public RankInPref rankOf(Project project)
		{
			return theOne.rankOf(project);
		}
	}
}
