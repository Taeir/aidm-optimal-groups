package nl.tudelft.aidm.optimalgroups.model.pref.base;

import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Collections;
import java.util.List;

// NOTE: Prefs do not link to DatasetContext?

public class ListBasedProjectPreferences extends AbstractListBasedProjectPreferences
{
	private final Object owner;
	private final List<Project> preferencesAsList;

	public ListBasedProjectPreferences(Object owner, List<Project> preferencesAsList)
	{
		this.owner = owner;
		this.preferencesAsList = preferencesAsList;
	}

	@Override
	public Object owner()
	{
		return owner;
	}

	@Override
	public List<Project> asListOfProjects()
	{
		return Collections.unmodifiableList(preferencesAsList);
	}
}