package nl.tudelft.aidm.optimalgroups.experiment.researchproj;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.partial.GroupsFromCliques;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.GroupConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.UndominatedByProfileConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ChiarandiniAgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Profile;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.MinimizeSumOfRanks;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.FormedGroupToProjectMatching;
import plouchtch.assertion.Assert;

import java.io.File;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class Experiment_two_round_groups_undom_individuals
{

	public static void main(String[] args)
	{
		var datasetContext = datasetCE10();
		
		var seqDatasetContext = SequentualDatasetContext.from(datasetContext);
		var allAgents = seqDatasetContext.allAgents();
//
//			var algo = new Chiarandini_Utilitarian_MinSum_IdentityScheme();
		
		var cliques = new GroupsFromCliques(allAgents);
		
		var maxsizeCliques = cliques.asCollection().stream()
			                     .filter(tentativeGroup -> tentativeGroup.members().count() == seqDatasetContext.groupSizeConstraint().maxSize())
			                     .collect(collectingAndThen(toList(), Groups.ListBackedImpl<Group.TentativeGroup>::new));
		
		// Indifferent agents don't care, don't include them in the profile as they consider any project to be equal.
		var groupingAgents = maxsizeCliques.asAgents();
		var indifferentAgents = allAgents.asCollection().stream().filter(agent -> agent.projectPreference().isCompletelyIndifferent()).collect(collectingAndThen(toList(), Agents::from));
		var individualAgents = allAgents.without(groupingAgents).without(indifferentAgents);
		
		
		try {
			var env = new GRBEnv();
			env.start();
			var model = new GRBModel(env);
			
			AssignmentConstraints assignmentConstraints = AssignmentConstraints.createInModel(model, seqDatasetContext);
			
			var objFn = new MinimizeSumOfRanks(seqDatasetContext, assignmentConstraints);
			objFn.apply(model);
			
			model.optimize();
			
			var matching = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDatasetContext).sequential();
			var profileIndividual = profileOfIndividualAgentsInMatching(seqDatasetContext, individualAgents, matching);
			
			var grpConstr = new GroupConstraint(assignmentConstraints, maxsizeCliques);
			grpConstr.apply(model);
			
			var domConstr = new UndominatedByProfileConstraint(assignmentConstraints, profileIndividual, individualAgents, seqDatasetContext.allProjects());
			domConstr.apply(model);
			
			model.update();
			model.optimize();
			
			var matching2 = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDatasetContext).sequential();
			var groupToProjectMatching = FormedGroupToProjectMatching.from(matching2);
			
			
			
			var report = new TwoRoundExperimentReport(matching, matching2, allAgents, individualAgents, groupingAgents, indifferentAgents);
			report.asHtmlReport()
				.writeHtmlSourceToFile(new File("reports/Experiment_2round_groups_undom_individuals.html"));
			
		}
		catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private static Profile.listBased profileOfIndividualAgentsInMatching(SequentualDatasetContext seqDatasetContext, Agents individualAgents, AgentToProjectMatching matching)
	{
		return matching.asList().stream()
			       // Only agents that are 'individual'
			       .filter(match -> individualAgents.findByAgentId(match.from().id).isPresent())
			       // A profile is a sorted list of ranks
			       .map(match -> {
				       var rank = match.from().projectPreference().rankOf(match.to());
				       Assert.that(rank.isPresent()).orThrowMessage("Rank not present, handle this case");
				       return rank.asInt();
			       })
			       .sorted()
			       .collect(collectingAndThen(toList(), Profile.listBased::new));
	}

	private static DatasetContext datasetCE10()
	{
		DatasetContext dataContext = CourseEdition.fromLocalBepSysDbSnapshot(10);
		return dataContext;
	}

}

