package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.profile.StudentRankProfile;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class Chiarandini_MinimaxOWA
{
	private final DatasetContext datasetContext;

	public Chiarandini_MinimaxOWA(DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
	}

	public AgentToProjectMatching doIt()
	{
		var objFn = new OWAObjective();
		
		return new ChiarandiniBaseModel(datasetContext, objFn).doIt();
	}

	/* test */
	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEditionFromDb.fromLocalBepSysDbSnapshot(10);

		var owaMinimaxChiarandini = new Chiarandini_MinimaxOWA(ce);
		var resultOwa = owaMinimaxChiarandini.doIt();

		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankProfile(resultOwa).displayChart("Chiarandini minimax-owa");

		return;
	}
	
	
}
