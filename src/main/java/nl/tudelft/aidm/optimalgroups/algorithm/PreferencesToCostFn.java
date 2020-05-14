package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

/**
 * Returns the cost of {@link Project} to agent whose preferences are given ({@link ProjectPreference})
 */
@FunctionalInterface
public interface PreferencesToCostFn
{
	Integer costOfGettingAssigned(ProjectPreference projectPreference, Project theProject);
}
