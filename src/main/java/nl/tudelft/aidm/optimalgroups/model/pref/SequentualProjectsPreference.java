package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.metric.rank.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualProjects;
import plouchtch.assertion.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

public class SequentualProjectsPreference implements ProjectPreference
{
	private final Integer[] preferenceProfile;
	private List<Project> preferenceProfileAsProjectList = null;

	public static SequentualProjectsPreference fromOriginal(ProjectPreference projectPreference, SequentualProjects sequentualProjects)
	{
		var originalProfile = projectPreference.asArray();
		var remappedProfile = new Integer[originalProfile.length];

//		projectPreference.forEach((Project project, OptionalInt rank) -> {
//			var origProject = project;
//
//			// rank 1 = index 0
//			for (int i = 0; i < ori; i++)
//			{
//
//			}
//			remappedProfile[rank-1] = remapped.id();
//		});

		var originalProfileAsList = projectPreference.asListOfProjects();
		for (int i = 0; i < originalProfileAsList.size(); i++)
		{

			Project origProject = originalProfileAsList.get(i);
			var remapped = sequentualProjects.correspondingSequentualProjectOf(origProject);
			remappedProfile[i] = remapped.id();
		}

		return new SequentualProjectsPreference(remappedProfile);
	}

	public SequentualProjectsPreference(Integer[] preferenceProfile)
	{
		this.preferenceProfile = preferenceProfile;
	}

	@Override
	public Integer[] asArray()
	{
		return preferenceProfile;
	}

	@Override
	public List<Project> asListOfProjects()
	{
		throw new RuntimeException("IMPL ME");
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
