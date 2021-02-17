package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRB;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AgentToProjectMatching implements nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching
{
	private final List<Match<Agent, Project>> asList;
	private final DatasetContext datasetContext;

	public AgentToProjectMatching(AssignmentVariablesAndConstraints assignmentVariablesAndConstraints, GRBModel model, SequentualDatasetContext datasetContext)
	{
		var matches = new ArrayList<Match<Agent, Project>>();

		datasetContext.allAgents().forEach(agent ->
		{
			datasetContext.allProjects().forEach(project ->
			{
				project.slots().forEach(slot ->
				{
					var x = assignmentVariablesAndConstraints.x(agent, project, slot);
					if (x != null)
					{
						var xValue = Try.getting(() -> x.asVar().get(GRB.DoubleAttr.X))
							.or(Rethrow.asRuntime());

						if (xValue > 0.9)
						{
							var ogAgent = datasetContext.mapToOriginal(agent);
							var ogProject = datasetContext.mapToOriginal(project);
							var match = new AgentToProjectMatch(ogAgent, ogProject);
							matches.add(match);
						}
					}
				});
			});
		});

		this.asList = Collections.unmodifiableList(matches);
		this.datasetContext = datasetContext.originalContext();
	}

	@Override
	public List<Match<Agent, Project>> asList()
	{
		return this.asList;
	}

	@Override
	public DatasetContext datasetContext()
	{
		return datasetContext;
	}
}
