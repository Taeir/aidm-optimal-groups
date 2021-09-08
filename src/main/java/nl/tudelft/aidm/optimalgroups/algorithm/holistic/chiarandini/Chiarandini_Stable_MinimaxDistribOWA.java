package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.StabilityConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ChiarandiniAgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.MinimizeSumOfExpRanks;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.profile.StudentRankProfile;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public class Chiarandini_Stable_MinimaxDistribOWA
{
	private final DatasetContext datasetContext;

	public Chiarandini_Stable_MinimaxDistribOWA(DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
	}

	public AgentToProjectMatching doIt()
	{
		var objFn = new OWAObjective();
		var stability = new StabilityConstraint();
		
		return new ChiarandiniBaseModel(datasetContext, objFn, stability).doIt();
	}
	
	/* test */
	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEditionFromDb.fromLocalBepSysDbSnapshot(10);


		var owaMinimaxChiarandini = new Chiarandini_MinimaxOWA(ce);
		var resultOwa = owaMinimaxChiarandini.doIt();

		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankProfile(resultOwa).displayChart("Chiarandini minimax-owa");


		var stableOwaMinimaxChiarandini = new Chiarandini_Stable_MinimaxDistribOWA(ce);
		var resultStableOwa = stableOwaMinimaxChiarandini.doIt();

		var metricsStableOwa = new MatchingMetrics.StudentProject(resultStableOwa);
		new StudentRankProfile(resultStableOwa).displayChart("Chiarandini stable_minimax-owa");

		return;
	}

}
