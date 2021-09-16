package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.StabilityConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.profile.StudentRankProfile;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class Chiarandini_Stable_MinimaxDistribOWA
{
	private final DatasetContext datasetContext;
	private final Pregrouping pregrouping;

	public Chiarandini_Stable_MinimaxDistribOWA(DatasetContext datasetContext, PregroupingType pregroupingType)
	{
		this.datasetContext = datasetContext;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
	}

	public AgentToProjectMatching doIt()
	{
		var objFn = new OWAObjective();
		var stability = new StabilityConstraint();
		
		return new ChiarandiniBaseModel(datasetContext, objFn, pregrouping.constraint(), stability).doIt();
	}
	
	/* test */
	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEditionFromDb.fromLocalBepSysDbSnapshot(10);


		var owaMinimaxChiarandini = new Chiarandini_MinimaxOWA(ce, PregroupingType.anyCliqueHardGrouped());
		var resultOwa = owaMinimaxChiarandini.doIt();

		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankProfile(resultOwa).displayChart("Chiarandini minimax-owa");


		var stableOwaMinimaxChiarandini = new Chiarandini_Stable_MinimaxDistribOWA(ce, PregroupingType.anyCliqueHardGrouped());
		var resultStableOwa = stableOwaMinimaxChiarandini.doIt();

		var metricsStableOwa = new MatchingMetrics.StudentProject(resultStableOwa);
		new StudentRankProfile(resultStableOwa).displayChart("Chiarandini stable_minimax-owa");

		return;
	}

}
