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

	// Indicates the rank (inclusive) from which the agent is indifferent
	// so all preferences of that rank and higher have the same rank, that
	// of the value of the field.
	private Integer isIndifferentFromRank = 0;

	public ProjectPreferencesInDb(DataSource dataSource, Integer userId, CourseEdition courseEdition)
	{
		this.dataSource = dataSource;
		this.userId = userId;
		this.courseEdition = courseEdition;
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

	@Override
	public OptionalInt rankOf(Project project)
	{
		if (isCompletelyIndifferent()) {
			return OptionalInt.empty();
		}

		var rankInArray = new RankInArray().determineRank(project.id(), asArray());

		if (rankInArray.isPresent()) {
			// Rank is present, clamp it to proper rank (indifference ranks are all 1 rank)
			var clamped = Math.min(rankInArray.getAsInt(), isIndifferentFromRank);
			return OptionalInt.of(clamped);
		}
		else {
			return OptionalInt.empty();
		}
	}

	@Override
	public Integer[] asArray()
	{
		if (preferences == null)
		{
			var submittedPreferencesOfAgentAsProjectIds = fetchFromDb().stream()
				.mapToInt(Integer::intValue)
				.toArray();

			var projectsInSubmittedPreferences = Projects.from(
				Arrays.stream(submittedPreferencesOfAgentAsProjectIds).boxed()
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
			var rnd = new Random(Arrays.hashCode(submittedPreferencesOfAgentAsProjectIds));
			Collections.shuffle(shuffledMissingProjects, rnd);

			// Join the two lists/arrays using streams into a single array - Java has no native Array.join
			preferences = Stream.concat(
					Arrays.stream(submittedPreferencesOfAgentAsProjectIds).boxed(),
					shuffledMissingProjects.stream().map(Project::id)
				)
				.toArray(Integer[]::new);

			// Rank is 1-based. agent is indifferent between all the projects that were just appended
			// that it did not originally submit
			isIndifferentFromRank = submittedPreferencesOfAgentAsProjectIds.length + 1;
		}

		return preferences;
	}

	@Override
	public synchronized List<Project> asListOfProjects()
	{
		if (preferencesAsProjectList == null) {
			var projectIdsInOrder = asArray();

			var allProjects = courseEdition.allProjects();
			var projectList = new ArrayList<Project>(projectIdsInOrder.length);

			for (var projId : projectIdsInOrder) {
				projectList.add(allProjects.findWithId(projId).get());
			}

			preferencesAsProjectList = Collections.unmodifiableList(projectList);

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
