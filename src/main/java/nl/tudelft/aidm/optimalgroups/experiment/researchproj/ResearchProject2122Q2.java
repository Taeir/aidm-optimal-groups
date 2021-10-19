package nl.tudelft.aidm.optimalgroups.experiment.researchproj;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.Algorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.partial.CliqueGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.MILP_Mechanism_FairPregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.FixMatchingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.UndominatedByProfileConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.HardGroupingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ChiarandiniAgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Profile;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.experiment.dataset.ResearchProject2021Q4Dataset;
import nl.tudelft.aidm.optimalgroups.experiment.dataset.ResearchProject2122Q2Dataset;
import nl.tudelft.aidm.optimalgroups.experiment.paper.fairness.report.FairnessVsVanillaQualityExperimentReport;
import nl.tudelft.aidm.optimalgroups.export.ProjectStudentMatchingCSV;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.ManualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.matchfix.MatchFixes;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.FormedGroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("DuplicatedCode")
public class ResearchProject2122Q2
{
	public static void main(String[] args)
	{
		var datasetContext = ResearchProject2122Q2Dataset.getInstance();
		
		var pregroupingType = PregroupingType.sizedCliqueSoftGrouped(datasetContext.groupSizeConstraint().maxSize());
		var pregrouping = pregroupingType.instantiateFor(datasetContext);
		
		var allAgents = datasetContext.allAgents();
//
//			var algo = new Chiarandini_Utilitarian_MinSum_IdentityScheme();
		
//		var maxsizeCliques = new CliqueGroups(allAgents).ofSize(datasetContext.groupSizeConstraint().maxSize());
		
		// Indifferent agents don't care, don't include them in the profile as they consider any project to be equal.
		var groupingAgents = pregrouping.groups().asAgents();
		var indifferentAgents = allAgents.asCollection().stream().filter(agent -> agent.projectPreference().isCompletelyIndifferent()).collect(collectingAndThen(toList(), Agents::from));
		var individualAgents = allAgents.without(groupingAgents).without(indifferentAgents);
		
		// dgb info
//		var values = maxsizeCliques.asCollection().stream().map(group -> {
//			return group.members().asCollection().stream()
//				       .map(agent -> agent.sequenceNumber().toString())
//				       .collect(Collectors.joining(", ", "[", "]"));
//		}).collect(Collectors.joining("\n"));
		
		var matchFixes = datasetContext.matchesToFix().asList().stream().flatMap(matchFix -> FixMatchingConstraint.from(matchFix).stream()).toArray(FixMatchingConstraint[]::new);
		
		var obj = new OWAObjective();
		
		var matchingFair = new MILP_Mechanism_FairPregrouping(
				datasetContext,
				obj,
				pregroupingType,
				matchFixes
			).doIt();
			
		var fileName = "research_proj21-22Q2 14_10_21";
		
		// EXPORT RESULTS
		var csv = new ProjectStudentMatchingCSV(matchingFair.finalMatching());
		csv.writeToFile("research_project/" + fileName);
		
			
		var report = new FairnessVsVanillaQualityExperimentReport(datasetContext, pregrouping, List.of(
				new GroupProjectAlgorithm.Result(new GroupProjectAlgorithm.Chiarandini_Fairgroups(obj, pregroupingType), matchingFair)
		));
			
		report.writeAsHtmlToFile(new File("reports/research_project/" + fileName + ".html"));
	}
	
	// TODO! If fix is a group, they must end up matched together in the same group
	private static void applyManualMatchFixes(MatchFixes matchFixes, GRBModel model, AssignmentConstraints assignmentConstraints) throws GRBException
	{
		for (var matchFix : matchFixes.asList())
		{
			if (matchFix.group().members().count() > 1) {
				new HardGroupingConstraint(Groups.of(matchFix.group()))
					.apply(model, assignmentConstraints);
			}
			
			for (var agent : matchFix.group().members())
			{
				var matchFixConstraint = new FixMatchingConstraint(agent, matchFix.project());
				matchFixConstraint.apply(model, assignmentConstraints);
			}
		}
	}
}

