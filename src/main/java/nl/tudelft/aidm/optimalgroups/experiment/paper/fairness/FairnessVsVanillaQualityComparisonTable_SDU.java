package nl.tudelft.aidm.optimalgroups.experiment.paper.fairness;

import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.chiarandini.SDUDatasetContext;
import nl.tudelft.aidm.optimalgroups.experiment.paper.fairness.report.FairnessVsVanillaQualitySummaryTableReport;
import nl.tudelft.aidm.optimalgroups.experiment.viz.FairnessComparisonsTable;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FairnessVsVanillaQualityComparisonTable_SDU
{
	public static void main(String[] args)
	{
		var experimentsRunId = Instant.now().getEpochSecond();
		
		var pregroupingType = PregroupingType.anyCliqueHardGrouped();
		
		var fairnessAlgo = new GroupProjectAlgorithm.Chiarandini_Fairgroups(new OWAObjective(), pregroupingType);
		var vanillAlgo = new GroupProjectAlgorithm.Chiarandini_MiniMax_OWA(pregroupingType);
		
		var datasets = List.<SDUDatasetContext>of(
				SDUDatasetContext.instanceOfYear(2008),
				SDUDatasetContext.instanceOfYear(2009),
				SDUDatasetContext.instanceOfYear(2010),
				SDUDatasetContext.instanceOfYear(2011),
				SDUDatasetContext.instanceOfYear(2012),
				SDUDatasetContext.instanceOfYear(2013),
				SDUDatasetContext.instanceOfYear(2014),
				SDUDatasetContext.instanceOfYear(2015),
				SDUDatasetContext.instanceOfYear(2016)
		);
		
		var resultsAll = new ArrayList<FairnessComparisonsTable.Result>();
		var resultsSingles = new ArrayList<FairnessComparisonsTable.Result>();
		var resultsPregrouped = new ArrayList<FairnessComparisonsTable.Result>();
		var resultsPregroupingUnsatisfied = new ArrayList<FairnessComparisonsTable.Result>();
		
		for (var datasetContext : datasets)
		{
			var pregrouping = pregroupingType.instantiateFor(datasetContext);
			
			var matchingFairness = fairnessAlgo.determineMatching(datasetContext);
			var matchingVanilla = vanillAlgo.determineMatching(datasetContext);
			
			var resultAll = new FairnessComparisonsTable.Result(
					AgentToProjectMatching.from(matchingFairness),
					AgentToProjectMatching.from(matchingVanilla)
			);
			resultsAll.add(resultAll);
			
			// result into document
			var matchingFairnessByStudentTypes = MatchingByStudentTypes.from(matchingFairness, pregrouping);
			var matchingVanillaByStudentTypes = MatchingByStudentTypes.from(matchingVanilla, pregrouping);
			
			var resultSingles = new FairnessComparisonsTable.Result(matchingFairnessByStudentTypes.singles(), matchingVanillaByStudentTypes.singles());
			resultsSingles.add(resultSingles);
			
			var resultPregrouped = new FairnessComparisonsTable.Result(matchingFairnessByStudentTypes.pregrouped(), matchingVanillaByStudentTypes.pregrouped());
			resultsPregrouped.add(resultPregrouped);

			var resultPregroupingUnsat = new FairnessComparisonsTable.Result(matchingFairnessByStudentTypes.pregroupingUnsatisfied(), matchingVanillaByStudentTypes.pregroupingUnsatisfied());
			resultsPregroupingUnsatisfied.add(resultPregroupingUnsat);
			
		}
		
		var id  = "SDU";
		
		var fileName = String.format("comparison_fair_vanilla_%s_%s-%s", experimentsRunId, id, pregroupingType.simpleName());
		
		new FairnessVsVanillaQualitySummaryTableReport(pregroupingType, resultsAll, resultsSingles, resultsPregrouped, resultsPregroupingUnsatisfied)
				.writeAsHtmlToFile(new File("reports/thesis/" + fileName + ".html"));
	}
	
	record MatchingByStudentTypes(AgentToProjectMatching singles, AgentToProjectMatching pregrouped, AgentToProjectMatching pregroupingUnsatisfied)
	{
		public static MatchingByStudentTypes from(GroupToProjectMatching<?> matchingAll, Pregrouping pregrouping)
		{
			var preformedGroups = pregrouping.groups();
			var agentsPregrouping = pregrouping.groups().asAgents();
			var agentsSingle = matchingAll.datasetContext().allAgents().without(agentsPregrouping);
		
			var matchingIndividualsToProjects = AgentToProjectMatching.from(matchingAll);
			
			var matchingPregroupedSatisfied = AgentToProjectMatching.from( matchingAll.filteredBySubsets(preformedGroups) ).filteredBy(agentsPregrouping);
			
			var pregroupingStudentsSatisfied = matchingPregroupedSatisfied.agents();
			var pregroupingStudentsUnsatisfied = agentsPregrouping.without(pregroupingStudentsSatisfied);
			
			var matchingSingles = matchingIndividualsToProjects.filteredBy(agentsSingle);
			var matchingPregrouped = matchingIndividualsToProjects.filteredBy(agentsPregrouping);
			var matchingPregroupedUnsatisfied = matchingIndividualsToProjects.filteredBy(pregroupingStudentsUnsatisfied);
			
			return new MatchingByStudentTypes(matchingSingles, matchingPregrouped, matchingPregroupedUnsatisfied);
		}
	}
}
