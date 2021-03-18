package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ChiarandiniAgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.DistributiveWeightsObjective;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Profile;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.profile.StudentRankProfile;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

public class UndominatedStudentsProfile
{
	private final DatasetContext datasetContext;
	private final Profile profile;
	private final ObjectiveFunction objectiveFunction;
	
	public UndominatedStudentsProfile(DatasetContext datasetContext, Profile profile, ObjectiveFunction objectiveFunction)
	{
		this.datasetContext = datasetContext;
		this.profile = profile;
		this.objectiveFunction = objectiveFunction;
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
		
		objectiveFunction.apply(model);
		
		model.optimize();
		
		// extract x's and map to matching
		var matching = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDatasetContext);
		
		env.dispose();
		
		return matching.original();
	}
	
	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		
		var owaMinimaxChiarandini = new Chiarandini_MinimaxDistribOWA(ce);
		var resultOwa = owaMinimaxChiarandini.doIt();
		
		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankProfile(resultOwa).displayChart("Chiarandini minimax-owa");
		
		return;
	}
}
