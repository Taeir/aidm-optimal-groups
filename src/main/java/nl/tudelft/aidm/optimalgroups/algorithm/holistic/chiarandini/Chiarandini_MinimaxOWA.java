package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ChiarandiniAgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.profile.StudentRankProfile;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public class Chiarandini_MinimaxOWA
{
	private final DatasetContext datasetContext;

	public Chiarandini_MinimaxOWA(DatasetContext datasetContext)
	{
		this.datasetContext = datasetContext;
	}

	public AgentToProjectMatching doIt()
	{
		return Try.getting(this::doItDirty)
			.or(Rethrow.asRuntime());
	}

	public AgentToProjectMatching doItDirty() throws GRBException
	{
		var seqDatasetContext = SequentualDatasetContext.from(datasetContext);

		var env = new GRBEnv();
		env.start();

		var model = new GRBModel(env);

		AssignmentConstraints assignmentConstraints = AssignmentConstraints.createInModel(model, seqDatasetContext);
		
		var objFn = new OWAObjective(seqDatasetContext, assignmentConstraints);
		objFn.apply(model);

		model.optimize();

		// extract x's and map to matching
		var matching = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDatasetContext);

		env.dispose();

		return matching.original();
	}

	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);

		var owaMinimaxChiarandini = new Chiarandini_MinimaxOWA(ce);
		var resultOwa = owaMinimaxChiarandini.doIt();

		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankProfile(resultOwa).displayChart("Chiarandini minimax-owa");

		return;
	}
	
	
}
