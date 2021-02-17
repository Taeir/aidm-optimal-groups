package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.metric.rank.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualProjects;
import plouchtch.assertion.Assert;

import java.util.*;

public class SequentualProjectsPreference implements ProjectPreference
{
	private final Object owner;
	private final Integer[] preferenceProfile;
	private List<Project> preferenceProfileAsProjectList = null;

	public static SequentualProjectsPreference fromOriginal(Object owner, ProjectPreference projectPreference, SequentualProjects sequentualProjects)
	{
		var originalProfile = projectPreference.asArray();
		var remappedProfile = new Integer[originalProfile.length];
		var asList = new ArrayList<Project>(originalProfile.length);

		// Ensure indices 0 to length exist, so we can use list.set(index, project)
		for (int i = 0; i < originalProfile.length; i++)
			asList.add(null);

		var originalProfileAsList = projectPreference.asListOfProjects();
		for (int i = 0; i < originalProfileAsList.size(); i++)
		{
			Project origProject = originalProfileAsList.get(i);
			var remapped = sequentualProjects.correspondingSequentualProjectOf(origProject);

			remappedProfile[i] = remapped.id();
			asList.set(i, remapped);
		}

		return new SequentualProjectsPreference(owner, remappedProfile, asList);
	}

	private SequentualProjectsPreference(Object owner, Integer[] preferenceProfile, List<Project> profileAsList)
	{
		this.owner = owner;
		this.preferenceProfile = preferenceProfile;
		this.preferenceProfileAsProjectList = Collections.unmodifiableList(profileAsList);
	}

	@Override
	public Object owner()
	{
		return owner;
	}

	@Override
	public Integer[] asArray()
	{
		return preferenceProfile;
	}

	@Override
	public List<Project> asListOfProjects()
	{
		return preferenceProfileAsProjectList;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ProjectPreference)) return false;
		if (!(o instanceof SequentualProjectsPreference)) throw new RuntimeException("Hmm SequentualProjects is being compared with some other type. Check if use-case is alright.");
		ProjectPreference that = (ProjectPreference) o;
		return Arrays.equals(preferenceProfile, that.asArray());
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(preferenceProfile);
	}
}
