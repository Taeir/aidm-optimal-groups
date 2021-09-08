package nl.tudelft.aidm.optimalgroups.model.dataset.sequentual;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.SeqentialGroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.SequentualProjectsPreference;
import plouchtch.assertion.Assert;

import java.util.*;

public class SequentualAgents extends Agents
{
	private final DatasetContext originalDatasetContext;

	SequentualAgents(Agents agents, SequentualDatasetContext datasetContext, SequentualProjects sequentualProjects)
	{
		super(datasetContext, mapAgentIdsToSequence(agents.asCollection(), sequentualProjects, datasetContext));
		originalDatasetContext = agents.datasetContext;
	}

	public Agent correspondingOriginalAgentOf(Agent agent)
	{
		Assert.that(agent instanceof SequentualAgent)
			.orThrowMessage("Cannot determine original agent of given remapped one, given is not a SequentualAgent");

		return ((SequentualAgent) agent).original;
	}

	public SequentualAgent correspondingSeqAgentOf(Agent agent)
	{
		return (SequentualAgent) asCollection().stream().filter(seqAgent -> ((SequentualAgent) seqAgent).original == agent)
			.findFirst().orElseThrow();
	}

	private static LinkedHashSet<Agent> mapAgentIdsToSequence(Collection<Agent> original, SequentualProjects sequentualProjects, SequentualDatasetContext datasetContext)
	{
		var originalSorted = new ArrayList<>(original);
		originalSorted.sort(Comparator.comparing(agent -> agent.sequenceNumber));

		var resequenced = new LinkedHashSet<Agent>(original.size());

		final int START_INDEX = 1;

		int sequenceNumber = START_INDEX;
		for (var agent : originalSorted) {

			var reseq = SequentualAgent.fromOriginal(sequenceNumber, agent, sequentualProjects, datasetContext);
			resequenced.add(reseq);

			sequenceNumber += 1;
		}

		return resequenced;
	}

	public static class SequentualAgent extends Agent
	{
		private final Agent original;

		protected SequentualAgent(Integer id, Agent original, SequentualProjectsPreference projectPreference, GroupPreference groupPreference, DatasetContext context)
		{
			super(id, projectPreference, groupPreference, context);
			this.original = original;
		}

		public static SequentualAgent fromOriginal(Integer newId, Agent agent, SequentualProjects sequentualProjects, SequentualDatasetContext context)
		{
			var owner = "SeqAgent_"+newId;
			var sequentualProjectsPreference = SequentualProjectsPreference.fromOriginal(owner, agent.projectPreference(), sequentualProjects);
			var seqGroupPreference = SeqentialGroupPreference.fromOriginal(context, agent.groupPreference);

			return new SequentualAgent(newId, agent, sequentualProjectsPreference, seqGroupPreference, context);
		}
	}
}
