package nl.tudelft.aidm.optimalgroups.dataset.generated;

import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.apache.commons.math3.distribution.NormalDistribution;

public class LinearlyDistributedProjectPreferencesGenerator extends ProjectPreferencesFromDistributionGenerator
{
	/**
	 * Creates a new Linearly Distributed ProjectPreferences Generator. It generates ProjectPreferences
	 * where in the projects rank is related to the probability of that project.
	 * @param projects The projects for which the preferences are generated
	 * @param slopeSteepness The steepness of the slope - that is the z in y = z*x
	 */
	public LinearlyDistributedProjectPreferencesGenerator(Projects projects, double slopeSteepness)
	{
		super(projects, new NormalDistribution(projects.count(), projects.count() * slopeSteepness));
	}
}
