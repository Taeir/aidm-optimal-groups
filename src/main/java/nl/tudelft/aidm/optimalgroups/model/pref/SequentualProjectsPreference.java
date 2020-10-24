package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualProjects;
import plouchtch.assertion.Assert;

import java.util.Arrays;
import java.util.List;

public class SequentualProjectsPreference implements ProjectPreference
{
	private static ProjectPreference projectPreference;
	private static SequentualProjects sequentualProjects;

	private final Integer[] preferenceProfile;
	private List<Project> preferenceProfileAsProjectList = null;

	public static SequentualProjectsPreference fromOriginal(ProjectPreference projectPreference, SequentualProjects sequentualProjects)
	{
		SequentualProjectsPreference.projectPreference = projectPreference;
		SequentualProjectsPreference.sequentualProjects = sequentualProjects;
		var originalProfile = projectPreference.asArray();
		var remappedProfile = new Integer[originalProfile.length];

		projectPreference.forEach((int projectId, int rank) -> {
			var origProject = new Project.ProjectsWithDefaultSlotAmount(projectId);
			var remapped = sequentualProjects.correspondingSequentualProjectOf(origProject);

			// Warn developer when a non-default slots implementation is used because this function then needs to be rewritten
			Assert.that(remapped.original() instanceof Project.ProjectWithStaticSlotAmount)
				.orThrow(() -> new RuntimeException("ProjectPreferences cannot be remapped to seqentual projects, non-'withDefaultSlots' implementation is used. Needs new implementation!"));

			// rank 1 = index 0
			remappedProfile[rank-1] = remapped.id();
		});

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
