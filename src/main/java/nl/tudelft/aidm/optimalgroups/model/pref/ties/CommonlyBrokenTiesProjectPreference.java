package nl.tudelft.aidm.optimalgroups.model.pref.ties;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.base.ListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommonlyBrokenTiesProjectPreference extends ListBasedProjectPreferences
{
	public CommonlyBrokenTiesProjectPreference(ProjectPreference projectPreference, Projects projectsInCommonSeq)
	{
		super(augmentWithMissing(projectPreference.asListOfProjects(), projectsInCommonSeq));
	}

	private static List<Project> augmentWithMissing(List<Project> pref, Projects allProjects)
	{
		var absent = allProjects.without(Projects.from(pref)).asCollection();

		// Join the two lists
		var completePref = new ArrayList<Project>(allProjects.count());
		completePref.addAll(pref);
		completePref.addAll(absent);

		return completePref;
	}
}
