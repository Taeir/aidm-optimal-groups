package nl.tudelft.aidm.optimalgroups.experiment.agp;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.algorithm.AgentProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.partial.GroupsFromCliques;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.*;
import nl.tudelft.aidm.optimalgroups.dataset.DatasetContextTiesBrokenIndividually;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.generated.GeneratedDataContext;
import nl.tudelft.aidm.optimalgroups.dataset.generated.prefs.NormallyDistributedProjectPreferencesGenerator;
import nl.tudelft.aidm.optimalgroups.dataset.generated.prefs.UniformProjectPreferencesGenerator;
import nl.tudelft.aidm.optimalgroups.experiment.agp.report.ExperimentReportInHtml;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualAgents;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.FormedGroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Experiment_two_round_groups_undom_individuals
{
	static class TwoRoundGroups_undomIndividuals implements GroupProjectAlgorithm
	{
		@Override
		public String name()
		{
			return "Two round Chiarandini with groups - undominated individual students";
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var seqDatasetContext = SequentualDatasetContext.from(datasetContext);
			UtilitarianWeightsObjective.WeightScheme weightScheme = rank -> rank;

			var allAgents = seqDatasetContext.allAgents();
//
//			var algo = new Chiarandini_Utilitarian_MinSum_IdentityScheme();

			var cliques = new GroupsFromCliques(allAgents);

			var maxsizeCliques = cliques.asCollection().stream()
				.filter(tentativeGroup -> tentativeGroup.members().count() == seqDatasetContext.groupSizeConstraint().maxSize())
				.collect(collectingAndThen(toList(), Groups.ListBackedImpl<Group.TentativeGroup>::new));

			// Indifferent agents don't care, don't include them in the profile as they consider any project to be equal.
			var indifferent = allAgents.asCollection().stream().filter(agent -> agent.projectPreference().isCompletelyIndifferent()).collect(collectingAndThen(toList(), Agents::from));
			var individualAgents = allAgents.without(maxsizeCliques.asAgents()).without(indifferent);


			try {
				var env = new GRBEnv();
				env.start();
				var model = new GRBModel(env);

				AssignmentConstraints assignmentConstraints = AssignmentConstraints.createInModel(model, seqDatasetContext);
				UtilitarianWeightsObjective.createInModel(model, seqDatasetContext, assignmentConstraints, weightScheme);

				model.optimize();

				var matching = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDatasetContext);
				var profileIndividual = profileOfIndividualAgentsInMatching(seqDatasetContext, individualAgents, matching);

				var grpConstr = new GroupConstraint(assignmentConstraints, model, maxsizeCliques);
				var domConstr = new UndominatingConstraint(assignmentConstraints, model, profileIndividual, individualAgents, seqDatasetContext.allProjects());

				model.update();
				model.optimize();

				var matching2 = new ChiarandiniAgentToProjectMatching(assignmentConstraints.xVars, seqDatasetContext);
				var groupToProjectMatching = FormedGroupToProjectMatching.from(matching2);

				return groupToProjectMatching;
			}
			catch (GRBException e) {
				throw new RuntimeException(e);
			}
		}

		private Profile.listBased profileOfIndividualAgentsInMatching(SequentualDatasetContext seqDatasetContext, Agents individualAgents, ChiarandiniAgentToProjectMatching matching)
		{
			return matching.asList().stream()
				// The matching is with original Agents, Projects, so quickly remap to Sequentual
				.map(match -> new Match<Agent, Project>()
				{
					public Agent from()
					{
						return seqDatasetContext.allAgents().correspondingSeqAgentOf(match.from());
					}

					public Project to()
					{
						return seqDatasetContext.allProjects().correspondingSequentualProjectOf(match.to());
					}
				})
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
	}

	public static void main(String[] args)
	{
		var experimentsForInReport = new ArrayList<Experiment>();

		List<GroupProjectAlgorithm> algorithms = List.of(
			new TwoRoundGroups_undomIndividuals()
		);

		/*new ILPPP_TGAlgorithm()*/ // will not succeed on CE10

//		var groupSize = GroupSizeConstraint.manual(4, 5);

		/* CE 10 */
		experimentsForInReport.add(experimentCE10(algorithms));

		new ExperimentReportInHtml(experimentsForInReport)
			.writeHtmlSourceToFile(new File("reports/Experiment_2round_ groups_undom_individuals.html"));

		return;
	}

	private static Experiment experimentCE10(List<GroupProjectAlgorithm> algorithms)
	{
		DatasetContext dataContext = CourseEdition.fromLocalBepSysDbSnapshot(10);
		return new Experiment(dataContext, algorithms);
	}

	private static Experiment experimentThreeSlotsUniformPrefs40p(List<GroupProjectAlgorithm> algorithms, GroupSizeConstraint groupSize)
	{
		var numSlots = 3;
		var numProjects = 40;
		var numAgents = numProjects * groupSize.maxSize();

		var projects = Projects.generated(40, numSlots);
		var prefGenerator = new UniformProjectPreferencesGenerator(projects);
		var dataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		var experiment = new Experiment(dataContext, algorithms);
		return experiment;
	}

	private static Experiment experimentThreeSlotsCE10Like(List<GroupProjectAlgorithm> algorithms, GroupSizeConstraint groupSize)
	{
		var numSlots = 3;
		var numProjects = 40;
		var numAgents = numProjects * groupSize.maxSize();

		var projects = Projects.generated(40, numSlots);
		var prefGenerator = new NormallyDistributedProjectPreferencesGenerator(projects, 4);
		var dataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		var experiment = new Experiment(dataContext, algorithms);
		return experiment;
	}

	private static Experiment experimentSingleSlotTightMatchingCE10Like(List<GroupProjectAlgorithm> algorithms, GroupSizeConstraint groupSize)
	{
		var numSlots = 1;
		var numProjects = 40;
		var numAgents = numProjects * groupSize.maxSize();

		var projects = Projects.generated(40, numSlots);
		var prefGenerator = new NormallyDistributedProjectPreferencesGenerator(projects, 4);
		var dataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		var experiment = new Experiment(dataContext, algorithms);
		return experiment;
	}

	private static Experiment experimentCE4(List<GroupProjectAlgorithm> algorithms)
	{
		DatasetContext dataContext = CourseEdition.fromLocalBepSysDbSnapshot(4);

		var numSlots = 5;
		var numProjects = dataContext.allProjects().count();
		var numAgents = dataContext.allAgents().count();

		var projects = dataContext.allProjects();

		return new Experiment(dataContext, algorithms);
	}


}

