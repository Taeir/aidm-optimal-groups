package nl.tudelft.aidm.optimalgroups.model.dataset.sequentual;

import nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.util.*;

/**
 * <p>Use this when Project id's must be within range [1, n] - in other words, the id is equivalent to
 * an index in an array. Some algorithms (Google OR Tools: MinCost MaxFlow, MiniZinc etc) cannot deal
 * with arbitrary id's or identify the projects by a monotonically increasing integer range.
 *
 * <p>The algorithm is simple: sort the projects by their original id's and assign id according to the sequence
 * number of the original. Example: [1, 55, 65, 66, 101] becomes [1, 2, 3, 4, 5]
 */
public class SequentualProjects extends ListBasedProjects
{
	private final Projects original;
	private final List<Project> projectsInMonotonicSequence;

	private final Map<Project, SequentualProject> origToNewMap;

	public static SequentualProjects from(Projects projects)
	{
		if (projects instanceof SequentualProjects) {
			return (SequentualProjects) projects;
		}

		return new SequentualProjects(projects);
	}

	private SequentualProjects(Projects original)
	{
		super();
		this.original = original;

		projectsInMonotonicSequence = remapToSequentual(original);
		origToNewMap = correspondanceBetweenOriginalAndSequential(original.asCollection(), projectsInMonotonicSequence);
	}

	/**
	 * @return Projects with their original Id's, the Projects the SequentualIdProjects were based off
	 */
	public Projects asOriginal()
	{
		return original;
	}

	public SequentualProject correspondingSequentualProjectOf(Project project)
	{
		SequentualProject resequenced = origToNewMap.get(project);

		Assert.that(resequenced != null)
			.orThrow(() -> new RuntimeException(String.format("Project %s is not an 'original' in SequentualProjects", project)));

		return resequenced;
	}

	public Project correspondingOriginalProjectOf(Project project)
	{
		Assert.that(project instanceof SequentualProject)
			.orThrowMessage("Cannot determine original project of given remapped one, given is not a SequentualProject");

		return ((SequentualProject) project).original();
//		return projectsInMonotonicSequence.get(project.id());
	}

	@Override
	protected List<Project> projectList()
	{
		return projectsInMonotonicSequence;
	}

	public static class SequentualProject extends Project.ProjectsWithDefaultSlotAmount
	{
		private final Project originalProject;

		public SequentualProject(int id, Project originalProject)
		{
			super(id);
			this.originalProject = originalProject;
		}

		public Project original()
		{
			return originalProject;
		}

		@Override
		public String toString()
		{
			return String.format("Seq project %s, (orig: %s)", id(), originalProject.id());
		}
	}

	/**
	 * Turns any Projects into SequentualProjects
	 * @param projectsToResequence Projects that need to be reindexed to [1,n]
	 * @return Projects with id's in [1,n]
	 */
	private static List<Project> remapToSequentual(Projects projectsToResequence)
	{
		final int START_INDEX = 1;

		var originalSorted = new ArrayList<>(projectsToResequence.asCollection());
		originalSorted.sort(Comparator.comparing(Project::id));

		var remappedProjects = new ArrayList<Project>(originalSorted.size());

		int sequenceNumber = START_INDEX;
		for (var proj :	originalSorted) {

			var remappedProj = new SequentualProject(sequenceNumber, proj);
			remappedProjects.add(remappedProj);

			sequenceNumber += 1;
		}

		return remappedProjects;
	}

	private static Map<Project, SequentualProject> correspondanceBetweenOriginalAndSequential(Collection<Project> original, List<Project> remapped)
	{
		var mapping = new HashMap<Project, SequentualProject>();

		var originalSorted = new ArrayList<>(original);
		originalSorted.sort(Comparator.comparing(Project::id));

		for (int i = 0; i < originalSorted.size(); i++)
		{
			var originalProjectInOrder = originalSorted.get(i);
			var remappedProj = (SequentualProject) remapped.get(i);

			Assert.that(remappedProj.originalProject == originalProjectInOrder)
				.orThrow(RuntimeException.class, "Original project to remapped mismatch :/");

			mapping.put(originalProjectInOrder, remappedProj);
		}

		return mapping;
	}
}
