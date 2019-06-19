package nl.tudelft.aidm.optimalgroups.model.entity;

import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface Projects
{
	int count();
	int countAllSlots();

	List<Project.ProjectSlot> slotsForProject(int projectId);

	void forEach(Consumer<Project> fn);
	Projects without(Project project);

	Collection<Project> asCollection();

	default Optional<Project> findWithId(int projectId)
	{
		return asCollection().stream().filter(project -> project.id() == projectId).findAny();
	}

	static Projects from(List<Project> projects)
	{
		return new ListBasedProjectsImpl(new ArrayList<>(projects));
	}

	abstract class ListBasedProjects implements Projects
	{
		public int count()
		{
			return projectList().size();
		}

		abstract protected List<Project> projectList();

		@Override
		public void forEach(Consumer<Project> fn)
		{
			projectList().forEach(fn);
		}

		@Override
		public Projects without(Project toExclude)
		{
			return new FilteredProjects(this.projectList(), toExclude);
		}

		@Override
		public List<Project.ProjectSlot> slotsForProject(int projectId) {
			String projectName = "proj_" + String.valueOf(projectId);
			Project project = this.projectList().stream()
				.filter(p -> p.name().equals(projectName))
				.findAny().get();

			return project.slots();
		}

		private int numTotalSlots = -1;

		@Override
		public int countAllSlots()
		{
			// lazy eval
			if (numTotalSlots < 0)
			{
				numTotalSlots = projectList().stream()
					.map(project -> project.slots().size())
					.mapToInt(Integer::intValue)
					.sum();
			}

			return numTotalSlots;
		}

		@Override
		public Collection<Project> asCollection() { return projectList(); }

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (!(o instanceof ListBasedProjects))
			{
				return false;
			}
			ListBasedProjects that = (ListBasedProjects) o;
			return numTotalSlots == that.numTotalSlots && projectList().equals(that.projectList());
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(numTotalSlots);
		}
	}

	class ListBasedProjectsImpl extends ListBasedProjects
	{
		private final List<Project> projects;
		private final Map<Integer, Project> byId;

		ListBasedProjectsImpl(List<Project> projects)
		{
			this.projects = projects;

			this.byId = new HashMap<>();
			projects.forEach(project -> {
				byId.put(project.id(), project);
			});
		}

		@Override
		protected List<Project> projectList()
		{
			return projects;
		}

		@Override
		public Optional<Project> findWithId(int projectId)
		{
			Project value = byId.get(projectId);
			return Optional.ofNullable(value);
		}
	}

	class FilteredProjects extends Projects.ListBasedProjects
	{
		private final List<Project> projects;
		private final Project excluded;

		public FilteredProjects(List<Project> projects, Project excluded)
		{
			this.excluded = excluded;
			this.projects = projects.stream().filter(p -> !p.equals(excluded)).collect(Collectors.toList());
		}

		@Override
		protected List<Project> projectList()
		{
			return projects;
		}
	}

	class InDb extends ListBasedProjects
	{
		private DataSource dataSource;
		private int courseEditionId;

		private List<Project> projectList = null;
		private Map<Integer, Project> byId = null;

		private InDb(DataSource dataSource, int courseEditionId)
		{
			this.dataSource = dataSource;
			this.courseEditionId = courseEditionId;
		}

		@Override
		protected List<Project> projectList()
		{
			if (projectList == null)
			{
				projectList = fetchFromDb();
			}

			return projectList;
		}


		@Override
		public Optional<Project> findWithId(int projectId)
		{
			if (byId == null) {
				projectList().forEach(project -> {
					byId.put(project.id(), project);
				});
			}

			Project value = byId.get(projectId);
			return Optional.ofNullable(value);
		}

		private List<Project> fetchFromDb()
		{
			var sql = "SELECT id FROM projects where course_edition_id = :courseEditionId";
			try (var connection = new Sql2o(dataSource).open())
			{
				Query query = connection.createQuery(sql);
				query.addParameter("courseEditionId", courseEditionId);

				List<Project> projectsAsList = query.executeAndFetch(
					(ResultSetHandler<Project>) rs ->
						new Project.withDefaultSlots(rs.getInt("id"))
				);

				return projectsAsList;
			}
		}

	}

	public static Projects fromDb(DataSource dataSource, int courseEditionInt)
	{
		return new InDb(dataSource, courseEditionInt);
	}
}
