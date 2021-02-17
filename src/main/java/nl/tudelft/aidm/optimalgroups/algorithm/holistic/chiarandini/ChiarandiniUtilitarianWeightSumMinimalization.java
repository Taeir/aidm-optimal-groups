package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.rank.distribution.StudentRankDistributionInMatching;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChiarandiniUtilitarianWeightSumMinimalization
{
	private final DatasetContext datasetContext;
	private UtilitarianWeightsObjective.WeightScheme weightScheme;

	public static ChiarandiniUtilitarianWeightSumMinimalization withIdentityWeightScheme(DatasetContext datasetContext)
	{
		return new ChiarandiniUtilitarianWeightSumMinimalization(datasetContext, rank -> rank);
	}

	public static ChiarandiniUtilitarianWeightSumMinimalization withExpWeightScheme(DatasetContext datasetContext, int maxRank)
	{
		UtilitarianWeightsObjective.WeightScheme weightScheme = rank -> - Math.pow(2, Math.max(0, maxRank - rank));

		return new ChiarandiniUtilitarianWeightSumMinimalization(datasetContext, weightScheme);
	}

	public ChiarandiniUtilitarianWeightSumMinimalization(DatasetContext datasetContext, UtilitarianWeightsObjective.WeightScheme weightScheme)
	{
		this.datasetContext = datasetContext;
		this.weightScheme = weightScheme;
	}

	public AgentToProjectMatching doIt() throws GRBException
	{
		var seqDatasetContext = SequentualDatasetContext.from(datasetContext);

		var env = new GRBEnv();
		env.start();

		var model = new GRBModel(env);

		AssignmentVariablesAndConstraints assignmentVariablesAndConstraints = AssignmentVariablesAndConstraints.createInModel(model, seqDatasetContext);
		UtilitarianWeightsObjective.createInModel(model, seqDatasetContext, assignmentVariablesAndConstraints, weightScheme);

		model.optimize();

		// extract x's and map to matching

		var matching = new ChiarandiniAgentToProjectMatching(assignmentVariablesAndConstraints, model, seqDatasetContext);

		env.dispose();

		return matching;
	}

	public static class ChiarandiniAgentToProjectMatching implements AgentToProjectMatching
	{
		private final List<Match<Agent, Project>> asList;
		private final DatasetContext datasetContext;

		public ChiarandiniAgentToProjectMatching(AssignmentVariablesAndConstraints assignmentVariablesAndConstraints, GRBModel model, SequentualDatasetContext datasetContext)
		{
			var matches = new ArrayList<Match<Agent, Project>>();

			datasetContext.allAgents().forEach(agent ->
			{
				datasetContext.allProjects().forEach(project ->
				{
					project.slots().forEach(slot ->
					{
						var x = assignmentVariablesAndConstraints.x(agent, project, slot);
						if (x != null) {
							var xValue = Try.getting(() -> x.asVar().get(GRB.DoubleAttr.X))
								.or(Rethrow.asRuntime());

							if (xValue > 0.9) {
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

	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		var utilitarianChiarandini = ChiarandiniUtilitarianWeightSumMinimalization.withIdentityWeightScheme(ce);
		var result = utilitarianChiarandini.doIt();

		var metrics = new MatchingMetrics.StudentProject(result);
		new StudentRankDistributionInMatching(result).displayChart();

		return;
	}

}
