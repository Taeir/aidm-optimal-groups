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
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

public class Chiarandini_Stable_MinimaxDistribOWA
{
	private final DatasetContext datasetContext;
	private final Pregrouping pregrouping;

	public Chiarandini_Stable_MinimaxDistribOWA(DatasetContext datasetContext, PregroupingType pregroupingType)
	{
		this.datasetContext = datasetContext;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
	}

	public GroupToProjectMatching<Group.FormedGroup> doIt()
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
		var agentToProjectMatching = AgentToProjectMatching.from(resultOwa);

		var metricsOwa = new MatchingMetrics.StudentProject(agentToProjectMatching);
		new StudentRankProfile(agentToProjectMatching).displayChart("Chiarandini minimax-owa");


		var stableOwaMinimaxChiarandini = new Chiarandini_Stable_MinimaxDistribOWA(ce, PregroupingType.anyCliqueHardGrouped());
		var resultStableOwa = stableOwaMinimaxChiarandini.doIt();
		
		var agentProjectMatching = AgentToProjectMatching.from(resultStableOwa);

		var metricsStableOwa = new MatchingMetrics.StudentProject(agentProjectMatching);
		new StudentRankProfile(agentProjectMatching).displayChart("Chiarandini stable_minimax-owa");

		return;
	}

}
