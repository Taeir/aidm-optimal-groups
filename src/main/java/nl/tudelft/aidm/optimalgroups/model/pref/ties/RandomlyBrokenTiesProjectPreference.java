package nl.tudelft.aidm.optimalgroups.model.pref.ties;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.base.AbstractListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.pref.base.ListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.lang.exception.ImplementMe;

import java.util.*;
import java.util.stream.Stream;

public class RandomlyBrokenTiesProjectPreference extends ListBasedProjectPreferences
{
	public RandomlyBrokenTiesProjectPreference(ProjectPreference projectPreference, Projects projects, long seed)
	{
		super(augmentWithMissing(projectPreference.asListOfProjects(), projects, seed));
	}

	private static List<Project> augmentWithMissing(List<Project> pref, Projects allProjects, long seed)
	{
		var absent = allProjects.without(Projects.from(pref)).asCollection();

		// Shuffle the projects that are absent from pref
		var absentCopy = new ArrayList<>(absent);
		var rnd = new Random(seed);
		Collections.shuffle(absentCopy, rnd);

		var absentShuffled = absentCopy;

		// Join the two lists
		var completePref = new ArrayList<Project>(allProjects.count());
		completePref.addAll(pref);
		completePref.addAll(absentShuffled);

		return completePref;
	}
}
