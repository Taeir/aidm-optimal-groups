package nl.tudelft.aidm.optimalgroups.model.pref.complete;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.base.ListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Missing preferences are appended individually ranomly
 */
public class ProjectPreferenceAugmentedWithMissingAlternativesIndvdRnd extends ListBasedProjectPreferences
{
	public ProjectPreferenceAugmentedWithMissingAlternativesIndvdRnd(ProjectPreference projectPreference, Projects projects, long seed)
	{
		super(
			projectPreference.owner(),
			augmentWithMissing(projectPreference.asList(), projects, seed)
		);
	}

	private static List<Project> augmentWithMissing(List<Project> pref, Projects allProjects, long seed)
	{
		var absent = allProjects.without(Projects.from(pref)).asCollection();

		// Shuffle the projects that are absent from pref
		var absentCopy = new ArrayList<>(absent);
		var rnd = new Random(seed * pref.hashCode());
		Collections.shuffle(absentCopy, rnd);

		var absentShuffled = absentCopy;

		// Join the two lists
		var completePref = new ArrayList<Project>(allProjects.count());
		completePref.addAll(pref);
		completePref.addAll(absentShuffled);

		return completePref;
	}
}
