package nl.tudelft.aidm.optimalgroups.dataset.generated;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleSinglePeakedGenerator implements PreferenceGenerator
{
	private final Projects projects;
	private final RandomDataGenerator dataGenerator;
	private final List<Pair<Project, Double>> pmf;

	public SimpleSinglePeakedGenerator(Projects projects)
	{
		this.projects = projects;
		this.dataGenerator = new RandomDataGenerator();

		// Normal distrib
		// Assign each project a prob from the distrib
		// Make EnumeratedDistribution with the <proj,p> pairs
		// Preference = sampler

		var distrib = new NormalDistribution(0.5, 0.2);

		this.pmf = projects.asCollection().stream()
			.map(project -> new Pair<>(project, distrib.sample()))
			.collect(Collectors.toList());

	}

	@Override
	public ProjectPreference generateNew()
	{
//		dataGenerator.
		return new GeneratedSinglePeakedProjectPreference(pmf);
	}

	class GeneratedSinglePeakedProjectPreference implements ProjectPreference
	{
		private final List<Project> prefProfile;
		private Integer[] asArray = null;

		public GeneratedSinglePeakedProjectPreference(List<Pair<Project, Double>> projPmf)
		{
			var pmf = new LinkedList<>(projPmf);
			var pref = new ArrayList<Project>(pmf.size());

			while (pmf.isEmpty() == false) {
				EnumeratedDistribution<Project> projectPrefDistrib = new EnumeratedDistribution<>(pmf);
				var sample = projectPrefDistrib.sample();
				pref.add(sample);

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
