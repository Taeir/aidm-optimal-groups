package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// NOTE: Prefs do not link to DatasetContext?

public class ListBasedProjectPreferences implements ProjectPreference
{
	private final List<Project> preferencesAsList;

	private Integer[] asArray = null;

	public ListBasedProjectPreferences(List<Project> preferencesAsList)
	{
		this.preferencesAsList = preferencesAsList;
	}

	@Override
	public Integer[] asArray()
	{
		if (asArray == null) {
			asArray = preferencesAsList.stream().map(Project::id).toArray(Integer[]::new);
		}

		return asArray;
	}

	@Override
	public List<Project> asListOfProjects()
	{
		return Collections.unmodifiableList(preferencesAsList);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ProjectPreference)) return false;
		if (!(o instanceof ListBasedProjectPreferences)) throw new RuntimeException("Hmm PrefProfPmf is being compared with some other type. Check if use-case is alright.");
		ListBasedProjectPreferences that = (ListBasedProjectPreferences) o;
		return preferencesAsList.equals(that.preferencesAsList);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(preferencesAsList);
	}
}