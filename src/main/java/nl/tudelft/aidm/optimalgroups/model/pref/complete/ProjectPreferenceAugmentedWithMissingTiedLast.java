package nl.tudelft.aidm.optimalgroups.model.pref.complete;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.base.ListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.PresentRankInPref;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.*;

/**
 * Missing preferences are appended individually ranomly
 */
public class ProjectPreferenceAugmentedWithMissingTiedLast extends ListBasedProjectPreferences
{
	private final Collection<Project> tiedAtEnd;
	private final Integer rankOfMissing;
	private final List<Project> asCompleteList;

	public ProjectPreferenceAugmentedWithMissingTiedLast(ProjectPreference projectPreference, Projects allProjects)
	{
		super(
			projectPreference.owner(),
			projectPreference.asList()
		);

		this.rankOfMissing = projectPreference.asList().size() + 1;

		var absent = allProjects.without(Projects.from(projectPreference.asList())).asCollection();
		tiedAtEnd = absent;

		var combined = new ArrayList<>(projectPreference.asList());
		combined.addAll(absent);
		asCompleteList = Collections.unmodifiableList(combined);
	}

	@Override
	public RankInPref rankOf(Project project)
	{
		var rankOriginalList = super.rankOf(project);

		if (rankOriginalList.unacceptable())
			return new PresentRankInPref(this.asList().size()+1);

		return rankOriginalList;
	}

	@Override
	public void forEach(ProjecPrefIterFn iter)
	{
		asCompleteList.forEach(project -> {
			iter.apply(project, rankOf(project));
		});
	}

	@Override
	public Integer[] asArray()
	{
		return asCompleteList.stream().map(Project::id).toArray(Integer[]::new);
	}

	@Override
	public Map<Integer, Integer> asMap()
	{
		var map = new HashMap<>(super.asMap());
		tiedAtEnd.forEach(missingProject -> {
			map.put(missingProject.id(), rankOfMissing);
		});

		return map;
	}

	@Override
	public List<Project> asList()
	{
		return this.asCompleteList;
	}
}
