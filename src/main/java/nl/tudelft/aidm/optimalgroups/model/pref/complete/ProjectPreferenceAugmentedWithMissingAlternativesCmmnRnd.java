package nl.tudelft.aidm.optimalgroups.model.pref.complete;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.base.ListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.ArrayList;
import java.util.List;

/**
 * Missing preferences are appended from a common ranking.
 *
 * That is if some two agents a1 and a2 are both missing projects some projects.
 * Say a1's list is [p5 p2] and a2's is [p4 p2]. Given the common ordering: [p5, p2, p4, p3, p1]
 *
 * Then both agents with be appended with the missing projects such
 * that their ordering is the same as that in the common ordering,
 * so a1's list will be [p5 p2 p4 p3 p1] and a2's [p4 p2 p5 p3 p1]
 */
public class ProjectPreferenceAugmentedWithMissingAlternativesCmmnRnd extends ListBasedProjectPreferences
{
	public ProjectPreferenceAugmentedWithMissingAlternativesCmmnRnd(ProjectPreference projectPreference, Projects projectsInCommonSeq)
	{
		super(
			augmentWithMissing(projectPreference.asList(), projectsInCommonSeq)
		);
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
