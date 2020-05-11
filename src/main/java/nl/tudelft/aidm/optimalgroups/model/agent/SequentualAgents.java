package nl.tudelft.aidm.optimalgroups.model.agent;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.dataset.RelabledCourseEditionContext;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.SequentualProjectsPreference;
import nl.tudelft.aidm.optimalgroups.model.project.SequentualProjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SequentualAgents extends Agents
{
	private SequentualAgents(Agents agents, SequentualProjects sequentualProjects)
	{
		super(mapAgentIdsToSequence(agents.asCollection(), sequentualProjects));
	}

	private static List<Agent> mapAgentIdsToSequence(Collection<Agent> original, SequentualProjects sequentualProjects)
	{
		var originalSorted = new ArrayList<>(original);
		originalSorted.sort(Comparator.comparing(agent -> agent.id));

		var resequenced = new ArrayList<Agent>(original.size());

		final int START_INDEX = 1;

		int sequenceNumber = START_INDEX;
		for (var agent : originalSorted) {

			var reseq = SequentualAgent.fromOriginal(sequenceNumber, agent, sequentualProjects);
			resequenced.add(reseq);

			sequenceNumber += 1;
		}

		return resequenced;
	}

	public static SequentualAgents from(Agents agents, SequentualProjects sequentualProjects)
	{
		return new SequentualAgents(agents, sequentualProjects);
	}

	public static class SequentualAgent extends Agent
	{
		private final Agent original;

		protected SequentualAgent(Integer id, Agent original, ProjectPreference projectPreference, GroupPreference groupPreference, DatasetContext context)
		{
			super(id, projectPreference, groupPreference, context);
			this.original = original;
		}

		public static SequentualAgent fromOriginal(Integer newId, Agent agent, SequentualProjects sequentualProjects)
		{
			var sequentualProjectsPreference = SequentualProjectsPreference.fromOriginal(agent.projectPreference, sequentualProjects);

			// fixme
			return new SequentualAgent(newId, agent, sequentualProjectsPreference, agent.groupPreference, new RelabledCourseEditionContext());
		}

		@Override
		public ProjectPreference projectPreference()
		{
			throw new RuntimeException("Group preferences not yet reampped to sequenced agents");
		}

		@Override
		public int groupPreferenceLength()
		{
			throw new RuntimeException("Group preferences not yet reampped to sequenced agents");
		}
	}
}
