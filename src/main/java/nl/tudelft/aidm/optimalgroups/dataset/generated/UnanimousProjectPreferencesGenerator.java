package nl.tudelft.aidm.optimalgroups.dataset.generated;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

/**
 * Identical preference shared by everybody!
 */
public class UnanimousProjectPreferencesGenerator implements PreferenceGenerator
{
	private final ProjectPreference preference;

	/**
	 * The unanimous preference is one drawn from the given generator
	 * @param source The generator from which the unanimous preference is drawn
	 */
	public UnanimousProjectPreferencesGenerator(PreferenceGenerator source)
	{
		this.preference = source.generateNew();
	}

	/**
	 * The given preference is the unanimous preference
	 * @param preference The unanimous preference
	 */
	public UnanimousProjectPreferencesGenerator(ProjectPreference preference)
	{
		this.preference = preference;
	}

	@Override
	public ProjectPreference generateNew()
	{
		return preference;
	}
}
