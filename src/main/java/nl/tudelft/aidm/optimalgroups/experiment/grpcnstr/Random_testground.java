package nl.tudelft.aidm.optimalgroups.experiment.grpcnstr;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.partial.CliqueGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.AssignmentConstraints;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.FixMatchingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.UndominatedByProfileConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.ConditionalGroupConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.HardGroupingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ChiarandiniAgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Profile;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.experiment.dataset.ResearchProject2021Q4Dataset;
import nl.tudelft.aidm.optimalgroups.experiment.researchproj.TwoRoundExperimentReport;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.matchfix.MatchFixes;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import plouchtch.assertion.Assert;
import plouchtch.functional.actions.Rethrow;
import plouchtch.util.Try;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class Random_testground
{
	public static void main(String[] args)
	{
//		var ce = 10;
//		var dataset = CourseEdition.fromLocalBepSysDbSnapshot(ce);
		
		var dataset = ResearchProject2021Q4Dataset.getInstance();
		var ce = dataset.courseEditionId();
		
		var seqDataset = SequentualDatasetContext.from(dataset);
		var allAgents = seqDataset.allAgents();
//
//			var algo = new Chiarandini_Utilitarian_MinSum_IdentityScheme();
		
		var maxsizeCliques = new CliqueGroups(allAgents).ofSize(seqDataset.groupSizeConstraint().maxSize());
		
		// Indifferent agents don't care, don't include them in the profile as they consider any project to be equal.
		var groupingAgents = maxsizeCliques.asAgents();
		var indifferentAgents = allAgents.asCollection().stream().filter(agent -> agent.projectPreference().isCompletelyIndifferent()).collect(collectingAndThen(toList(), Agents::from));
		var individualAgents = allAgents.without(groupingAgents).without(indifferentAgents);
		
		var values = maxsizeCliques.asCollection().stream().map(group -> {
			return group.members().asCollection().stream()
				       .map(agent -> seqDataset.mapToOriginal(agent))
				       .map(agent -> agent.id.toString())
				       .collect(Collectors.joining(", ", "[", "]"));
		}).collect(Collectors.joining("\n"));
		
		
		try {
			var env = new GRBEnv();
			env.start();
			var model = new GRBModel(env);
			
			/*         */
			/* ROUND 1 */
			/*         */
			AssignmentConstraints assignmentConstraints = AssignmentConstraints.createInModel(model, seqDataset);
			
			var objFn = new OWAObjective(seqDataset, assignmentConstraints);
			objFn.apply(model);
			
			// process match-fixes
//			var matchFixesSeq = dataset.matchesToFix().forSequentual(seqDataset);
//			applyManualMatchFixes(matchFixesSeq, model, assignmentConstraints);
			
			model.optimize();
			
			// results round 1
			var matching = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDataset);
			var profileIndividual = profileOfIndividualAgentsInMatching(seqDataset, individualAgents, matching.sequential());
			
			/*         */
			/* ROUND 2 */
			/*         */
			
//			var grpConstr = new GroupConstraint(maxsizeCliques);
//			var grpConstr = new SoftGroupConstraint(maxsizeCliques);
			var grpConstr = new ConditionalGroupConstraint(maxsizeCliques, 3);
			grpConstr.apply(model, assignmentConstraints);
			
			var domConstr = new UndominatedByProfileConstraint(profileIndividual, individualAgents, seqDataset.allProjects());
			domConstr.apply(model, assignmentConstraints);
			
			model.update();
			model.optimize();
			
			// results round 2
			var matching2 = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDataset);
			
			var groupingViolations = grpConstr.violateGroupingDecVars.stream()
				                        .map(ConditionalGroupConstraint.GrpLinkedDecisionVar::asVar)
				                        .map(grbVar -> Try.getting(() -> grbVar.get(GRB.DoubleAttr.X)).or(Rethrow.asRuntime()))
				                        .filter(v -> v > 0.0)
				                        .count();
//			var groupingViolations = 0;
			
			// assert all groups in final output
			var numCliquesTogether = new AtomicInteger(0);
			var matchingByProject = matching2.sequential().groupedByProject();
			maxsizeCliques.asCollection().forEach(clique ->{
				var cliqueIsTogether = matchingByProject.values().stream().anyMatch(agents -> agents.containsAll(clique.members().asCollection()));
				if (cliqueIsTogether) numCliquesTogether.incrementAndGet();
			});
			
			var matchingByRank = matching2.sequential().asList().stream()
					.collect(Collectors.groupingBy(o -> {
						var agent = o.from();
						var project = o.to();
						
						return agent.projectPreference().rankOf(project);
					}));
			
			Assert.that(numCliquesTogether.get() == (maxsizeCliques.count() - groupingViolations))
				.orThrowMessage("There is a clique not together, grp constr: " + grpConstr.simpleName());
			
			// EXPORT RESULTS
			Assert.that(dataset.numMaxSlots() == 1)
				.orThrowMessage("TODO: get mapping slot to agent (projects in dataset have more than 1 slot)");
//			var csv = new ProjectStudentMatchingCSV(FormedGroupToProjectMatching.fromByTrivialPartitioning(matching2.original()));
//			csv.writeToFile("research_project/research_proj " + objFn.name() + " 23_03_21 - w optional");
			
			
			var report = new TwoRoundExperimentReport(matching.sequential(), matching2.sequential(),
				seqDataset.allAgents(), individualAgents, groupingAgents, indifferentAgents);
			
			report.asHtmlReport()
				.writeHtmlSourceToFile(new File("reports/test/" + ce + "_" + grpConstr.simpleName() + "_" + objFn.name() + "" + ".html"));
			
		}
		catch (GRBException e) {
			throw new RuntimeException(e);
		}
	}
	
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
	
	private static DatasetContext datasetResearchProj21()
	{
		var dataContext = CourseEdition.fromLocalBepSysDbSnapshot(39);
		
		return dataContext;
	}

}

