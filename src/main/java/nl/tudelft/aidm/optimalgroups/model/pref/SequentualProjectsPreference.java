package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.SequentualProjects;
import plouchtch.assertion.Assert;

public class SequentualProjectsPreference implements ProjectPreference
{
	private final int[] preferenceProfile;

	public static SequentualProjectsPreference fromOriginal(ProjectPreference projectPreference, SequentualProjects sequentualProjects)
	{
		var originalProfile = projectPreference.asArray();
		var remappedProfile = new int[originalProfile.length];

		projectPreference.forEach((projectId, rank) -> {
			var origProject = new Project.withDefaultSlots(projectId);
			var remapped = sequentualProjects.correspondingSequentualProject(origProject);

			// Warn developer when a non-default slots implementation is used because this function then needs to be rewritten
			Assert.that(remapped.original() instanceof Project.withDefaultSlots)
				.orThrow(() -> new RuntimeException("ProjectPreferences cannot be remapped to seqentual projects, non-'withDefaultSlots' implementation is used. Needs new implementation!"));

			// rank 1 = index 0
			remappedProfile[rank-1] = remapped.id();
		});

		return new SequentualProjectsPreference(remappedProfile);
	}

	public SequentualProjectsPreference(int[] preferenceProfile)
	{
		this.preferenceProfile = preferenceProfile;
	}

	@Override
	public int[] asArray()
	{
		return preferenceProfile;
	}
}
