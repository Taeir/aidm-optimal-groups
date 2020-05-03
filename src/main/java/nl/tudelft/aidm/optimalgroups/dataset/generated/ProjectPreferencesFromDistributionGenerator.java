package nl.tudelft.aidm.optimalgroups.dataset.generated;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *  This type of generator generates ProjectPreferences
 * 	where in the projects rank is related to the probability of that project.
 * 	The probability is determined by the given distribution. More precisely, the projects correspond to [0,mean]
 * 	line of the PDF of the distribution. Mean = #number of projects.
 */
abstract class ProjectPreferencesFromDistributionGenerator implements PreferenceGenerator
{
	private final Projects projects;
	private final List<Pair<Project, Double>> pmf;

	/**
	 * Base constructor, given a RealDistribution the PMF of projects is constructed.
	 * @param projects The projects for which the preferences are generated
	 * @param distribution The distribution according to which the projects are drawn from
	 */
	public ProjectPreferencesFromDistributionGenerator(Projects projects, RealDistribution distribution)
	{
		this.projects = projects;

		// Assign each project a prob from the distrib
		//     - map projects from 0 to mean along the x-axis, but in an anonymous way
		// Make EnumeratedDistribution with the <Project,probability[0,1]> pairs

		var data = new ArrayList<>(projects.asCollection());
		Collections.shuffle(data);

		var pmf = new ArrayList<Pair<Project, Double>>(data.size());
		for (int i = 0; i < data.size(); i++)
		{
			var project = data.get(i);
			pmf.add(new Pair<>(project, distribution.density(i)));
		}

		this.pmf = pmf;
	}

	@Override
	public ProjectPreference generateNew()
	{
		// Would be nicer to have NormallyDistributedProjectPreference type...
		/// TODO: move PMF into own class
		return new PmfGeneratedProjectPreference(pmf);
	}

	class PmfGeneratedProjectPreference implements ProjectPreference
	{
		// The PMF used to generate this preference profile
		private final List<Pair<Project, Double>> completePmf;

		private final List<Project> prefProfile;
		private Integer[] asArray = null;

		public PmfGeneratedProjectPreference(List<Pair<Project, Double>> completePmf)
		{
			this.completePmf = completePmf;
			// We're building this list by drawing projects with probabilities defined in the PMF
			var pref = new ArrayList<Project>(pmf.size());

			// Copy the complete PMF
			var pmf = new LinkedList<>(completePmf);

			// While we have projects to add to our list
			while (pmf.isEmpty() == false) {
				EnumeratedDistribution<Project> projectPrefDistrib = new EnumeratedDistribution<>(pmf);
				var sample = projectPrefDistrib.sample();
				pref.add(sample);

				// Remove the project from PMF, the pmf will be updated with the remaining elements
				pmf.removeIf(pair -> pair.getKey().equals(sample));
			}

			prefProfile = pref;
		}

		@Override
		public Integer[] asArray()
		{
			if (asArray == null) {
				asArray = prefProfile.stream().map(Project::id).toArray(Integer[]::new);
			}

			return asArray;
		}

		@Override
		public List<Project> asListOfProjects()
		{
			return Collections.unmodifiableList(prefProfile);
		}
	}
}
