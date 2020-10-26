package nl.tudelft.aidm.optimalgroups.dataset.bepsys.pref;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.rank.RankInArray;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectPreferencesInDb implements ProjectPreference
{
	private final DataSource dataSource;
	private final Integer userId;
	private final CourseEdition courseEdition;

	private Integer[] preferences = null;
	private List<Project> preferencesAsProjectList = null;
	private Map<Integer, Integer> preferencesMap = null;

	private final IdentityHashMap<Project, OptionalInt> rankOfProject;

	// Indicates the rank (inclusive) from which the agent is indifferent
	// so all preferences of that rank and higher have the same rank, that
	// of the value of the field.
	private Integer isIndifferentFromRank = 0;

	public ProjectPreferencesInDb(DataSource dataSource, Integer userId, CourseEdition courseEdition)
	{
		this.dataSource = dataSource;
		this.userId = userId;
		this.courseEdition = courseEdition;
		this.rankOfProject = new IdentityHashMap<>();
	}

	@Override
	public boolean isCompletelyIndifferent()
	{
		if (isIndifferentFromRank == 0) {
			// force retrieval
			asArray();
		}

		return isIndifferentFromRank == 1;
	}

	// PREMATURE OPTIMIZATION: bypass rankOf
//	@Override
//	public void forEach(ProjectPreferenceObjectRankConsumer iter)
//	{
//		List<Project> projectList = asListOfProjects();
//		for (int rankInList = 1; rankInList <= projectList.size(); rankInList++)
//		{
//			int index = rankInList - 1;
//			var proj = projectList.get(index);
//
//			var rank = Math.min(rankInList, isIndifferentFromRank);
//			iter.apply(proj, Optional);
//		}
//	}

	@Override
	public OptionalInt rankOf(Project project)
	{
		// Cache results - pessimism makes heavy use of this fn
		return rankOfProject.computeIfAbsent(project, proj -> {

			if (isCompletelyIndifferent())
			{
				return OptionalInt.empty();
			}

			var rankInArray = new RankInArray().determineRank(proj.id(), asArray());

			if (rankInArray.isPresent())
			{
				// Rank is present, clamp it to proper rank (indifference ranks are all 1 rank)
				var clamped = Math.min(rankInArray.getAsInt(), isIndifferentFromRank);
				return OptionalInt.of(clamped);
			}
			else
			{
				return OptionalInt.empty();
			}

		});
	}

	@Override
	public synchronized Integer[] asArray()
	{
		if (preferences == null)
		{
			var submittedProjectPreferenceOfAgentAsArrayOfIds = fetchFromDb().stream()
				.mapToInt(Integer::intValue)
				.toArray();

			var projectsInSubmittedPreferences = Projects.from(
				Arrays.stream(submittedProjectPreferenceOfAgentAsArrayOfIds).boxed()
					.map(Project.ProjectsWithDefaultSlotAmount::new).collect(Collectors.toList())
			);

			var allProjects = courseEdition.allProjects();
			var projectsNotInPreferences = allProjects.without(projectsInSubmittedPreferences);

			// Append the missing projects in shuffled order. However, the shuffle can mess up the equals() method
			// we have two strategies: either 1) checkpoint the submitted prefs (with missing projs) and use those in equals
			// but that could complicate the equals method (do .asArray and then access the checkpoint field...)
			// or 2) ensure that the shuffle is deterministic for the submitted preferences (the chosen option).
			// Side note: it might have been better to move the "add missing projects to prefs" somewhere else...
			ArrayList<Project> shuffledMissingProjects = new ArrayList<>(projectsNotInPreferences.asCollection());
			var rnd = new Random(Arrays.hashCode(submittedProjectPreferenceOfAgentAsArrayOfIds));
			Collections.shuffle(shuffledMissingProjects, rnd);

			// Join the two lists/arrays using streams into a single array - Java has no native Array.join
			preferences = Stream.concat(
					Arrays.stream(submittedProjectPreferenceOfAgentAsArrayOfIds).boxed(),
					shuffledMissingProjects.stream().map(Project::id)
				).toArray(Integer[]::new);

			// Rank is 1-based. agent is indifferent between all the projects that were just appended
			// that it did not originally submit
			isIndifferentFromRank = submittedProjectPreferenceOfAgentAsArrayOfIds.length + 1;
		}

		return preferences;
	}

	@Override
	public synchronized List<Project> asListOfProjects()
	{
		if (preferencesAsProjectList == null) {
			var allProjects = courseEdition.allProjects();

			var linearlyOrderedProjectPref = asArray();
			var projectPrefAsList = new ArrayList<Project>(linearlyOrderedProjectPref.length);

			for (var projId : linearlyOrderedProjectPref) {
				var proj = allProjects.findWithId(projId);
				proj.ifPresent(projectPrefAsList::add);
			}

			preferencesAsProjectList = Collections.unmodifiableList(projectPrefAsList);
		}

		return preferencesAsProjectList;
	}

	@Override
	public String toString() {
		return "proj pref: " + Arrays.toString(asArray());
	}

	@Override
	public Map<Integer, Integer> asMap() {
		if (this.preferencesMap == null) {

			Integer[] preferences = this.asArray();
			this.preferencesMap = new HashMap<>(preferences.length);

			for (int rank = 1; rank <= preferences.length; rank++) {
				int project = preferences[rank - 1];
				this.preferencesMap.put(project, rank);
			}
		}

		return this.preferencesMap;
	}

	private List<Integer> fetchFromDb()
	{
		Sql2o sql2o = new Sql2o(dataSource);
		try (var conn = sql2o.open())
		{
			final var sql = "SELECT distinct(project_id) " +
				"FROM project_preferences " +
				"WHERE user_id = :userId AND course_edition_id = :courseEditionId " +
				"ORDER BY priority";

			Query query = conn.createQuery(sql)
				.addParameter("userId", userId)
				.addParameter("courseEditionId", courseEdition.bepSysId());

			List<Integer> prefs = query.executeAndFetch((ResultSetHandler<Integer>) resultSet -> resultSet.getInt("project_id"));
			return prefs;
		}
	}
}
