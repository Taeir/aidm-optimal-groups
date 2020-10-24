package nl.tudelft.aidm.optimalgroups.model.dataset.sequentual;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.dataset.RelabledCourseEditionContext;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.SequentualProjectsPreference;
import plouchtch.lang.exception.ImplementMe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SequentualAgents extends Agents
{
	private final DatasetContext originalDatasetContext;

	SequentualAgents(Agents agents, SequentualDataset datasetContext, SequentualProjects sequentualProjects)
	{
		super(datasetContext, mapAgentIdsToSequence(agents.asCollection(), sequentualProjects));
		originalDatasetContext = agents.datasetContext;
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
			var sequentualProjectsPreference = SequentualProjectsPreference.fromOriginal(agent.projectPreference(), sequentualProjects);

			// fixme
			return new SequentualAgent(newId, agent, sequentualProjectsPreference, agent.groupPreference, new RelabledCourseEditionContext());
		}

		@Override
		public int groupPreferenceLength()
		{
			throw new RuntimeException("Group preferences not yet reampped to sequenced agents");
		}
	}
}
