package nl.tudelft.aidm.optimalgroups.dataset.generated.prefs;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.function.Supplier;

public interface ProjectPreferenceGenerator
{
	ProjectPreference generateNew();
}
