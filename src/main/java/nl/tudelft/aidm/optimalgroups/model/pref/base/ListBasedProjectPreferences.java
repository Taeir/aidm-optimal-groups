package nl.tudelft.aidm.optimalgroups.model.pref.base;

import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Collections;
import java.util.List;

// NOTE: Prefs do not link to DatasetContext?

public class ListBasedProjectPreferences extends AbstractListBasedProjectPreferences
{
	private final List<Project> preferencesAsList;

	public ListBasedProjectPreferences(List<Project> preferencesAsList)
	{
		this.preferencesAsList = preferencesAsList;
	}

	@Override
	public List<Project> asListOfProjects()
	{
		return Collections.unmodifiableList(preferencesAsList);
	}
}