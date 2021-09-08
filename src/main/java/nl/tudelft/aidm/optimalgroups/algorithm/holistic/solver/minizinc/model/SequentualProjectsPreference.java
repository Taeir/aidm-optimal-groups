package nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver.minizinc.model;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.*;

public class SequentualProjectsPreference implements ProjectPreference
{
	private final Object owner;
	private final Project[] asArray;
	private List<Project> asList;

	public static SequentualProjectsPreference fromOriginal(Object owner, ProjectPreference projectPreference, SequentualProjects sequentualProjects)
	{
		var originalProfile = projectPreference.asArray();
		var remappedProfile = new Project[originalProfile.length];
		var asList = new ArrayList<Project>(originalProfile.length);

		// Ensure indices 0 to length exist, so we can use list.set(index, project)
		for (int i = 0; i < originalProfile.length; i++)
			asList.add(null);

		var originalProfileAsList = projectPreference.asList();
		for (int i = 0; i < originalProfileAsList.size(); i++)
		{
			Project origProject = originalProfileAsList.get(i);
			var remapped = sequentualProjects.correspondingSequentualProjectOf(origProject);

			remappedProfile[i] = remapped;
			asList.set(i, remapped);
		}

		return new SequentualProjectsPreference(owner, remappedProfile, asList);
	}

	private SequentualProjectsPreference(Object owner, Project[] asArray, List<Project> profileAsList)
	{
		this.owner = owner;
		this.asArray = asArray;
		this.asList = Collections.unmodifiableList(profileAsList);
	}

	@Override
	public Object owner()
	{
		return owner;
	}

	@Override
	public Project[] asArray()
	{
		return asArray;
	}

	@Override
	public List<Project> asList()
	{
		return asList;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ProjectPreference)) return false;
		if (!(o instanceof SequentualProjectsPreference)) throw new RuntimeException("Hmm SequentualProjects is being compared with some other type. Check if use-case is alright.");
		ProjectPreference that = (ProjectPreference) o;
		return Arrays.equals(asArray, that.asArray());
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(asArray);
	}
}
