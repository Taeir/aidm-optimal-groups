package nl.tudelft.aidm.optimalgroups.dataset.generated.prefs;

import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.apache.commons.math3.distribution.NormalDistribution;

public class NormallyDistributedProjectPreferencesGenerator extends ProjectPreferencesFromDistributionGenerator
{
	/**
	 * Creates a new Normally Distributed ProjectPreferences Generator. It generates ProjectPreferences
	 * where in the projects rank is related to the probability of that project.
	 * @param projects The projects for which the preferences are generated
	 * @param curveSteepness Used to calculate the stdDev, high values result in steep and narrow bells.
	 *                       In other words, narrows the projects that have high change of ending up top picks
	 *                       while the rest becomes "noise" or relatively uniform
	 */
	public NormallyDistributedProjectPreferencesGenerator(Projects projects, double curveSteepness)
	{
		super(projects, new NormalDistribution(projects.count(), projects.count() / curveSteepness));
	}
}
