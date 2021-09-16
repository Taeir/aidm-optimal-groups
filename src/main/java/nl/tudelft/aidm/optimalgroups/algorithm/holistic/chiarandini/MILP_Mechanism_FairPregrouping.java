package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.UndominatedByProfileConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Profile;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.profile.StudentRankProfile;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.List;

public class MILP_Mechanism_FairPregrouping
{
	private final DatasetContext datasetContext;
	private final ObjectiveFunction objectiveFunction;
	private final Pregrouping pregrouping;
	
	public MILP_Mechanism_FairPregrouping(DatasetContext datasetContext, ObjectiveFunction objectiveFunction, PregroupingType pregroupingType)
	{
		this.datasetContext = datasetContext;
		this.objectiveFunction = objectiveFunction;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
	}
	
	public Matching doIt()
	{
		var allAgents = datasetContext.allAgents();
		
		var preFormedGroups = pregrouping.groups();
		
		var pregroupingStudents = preFormedGroups.asAgents();
		var indifferentStudents = allAgents.asCollection().stream().filter(agent -> agent.projectPreference().isCompletelyIndifferent()).collect(Agents.collector);
		var singleStudents = allAgents.without(pregroupingStudents).without(indifferentStudents);

		// Initial matching to determine baseline outcome quality for the "single" students
		var baselineMatching = new ChiarandiniBaseModel(datasetContext, objectiveFunction)
				.doIt();
		
		var profile = Profile.of(baselineMatching, singleStudents);
		var paretoConstraint = new UndominatedByProfileConstraint(profile, singleStudents);
		
		var groupingConstraint = pregrouping.constraint();
		
		var finalMatching = new ChiarandiniBaseModel(datasetContext, objectiveFunction, paretoConstraint, groupingConstraint)
				.doIt();
		
		var matchingResults = new Matching(finalMatching, baselineMatching);
		
		return matchingResults;
	}
	
	public record Matching(AgentToProjectMatching finalMatching, AgentToProjectMatching baselineMatching) implements AgentToProjectMatching
	{
		public Matching
		{
			Assert.that(finalMatching.datasetContext() == baselineMatching.datasetContext()).orThrowMessage("dataset mismatch between baseline and final matching");
		}
		
		@Override
		public List<Match<Agent, Project>> asList()
		{
			return finalMatching.asList();
		}
		
		@Override
		public DatasetContext datasetContext()
		{
			return finalMatching.datasetContext();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEditionFromDb.fromLocalBepSysDbSnapshot(10);
		
		var owaMinimaxChiarandini = new Chiarandini_MinimaxOWA(ce, PregroupingType.anyCliqueHardGrouped());
		var resultOwa = owaMinimaxChiarandini.doIt();
		
		var metricsOwa = new MatchingMetrics.StudentProject(resultOwa);
		new StudentRankProfile(resultOwa).displayChart("Chiarandini minimax-owa");
		
		return;
	}
}
