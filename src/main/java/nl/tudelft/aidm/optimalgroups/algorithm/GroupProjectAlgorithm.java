package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.Algorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.BepSysReworked;
import nl.tudelft.aidm.optimalgroups.algorithm.group.CombinedPreferencesGreedy;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp.ILPPPDeterminedMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.WorstHumblePairingsSearch;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver.minizinc.GroupedProjectMinizincAllocation;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.spdc.SDPCOrderedByPotentialGroupmates;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.spdc.SerialDictatorshipWithProjClosures;
import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMaxFlow;
import nl.tudelft.aidm.optimalgroups.algorithm.project.RandomizedSerialDictatorship;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.FormedGroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.pref.AggregatedProfilePreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.Objects;
import java.util.function.Predicate;

public interface GroupProjectAlgorithm extends Algorithm
{
	GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext);

	class Result implements Algorithm.Result<GroupProjectAlgorithm, GroupToProjectMatching<Group.FormedGroup>>
	{
		private final GroupProjectAlgorithm algo;
		private final GroupToProjectMatching<Group.FormedGroup> result;

		public Result(GroupProjectAlgorithm algo, GroupToProjectMatching<Group.FormedGroup> result)
		{
			this.algo = algo;
			this.result = result;
		}

		@Override
		public Algorithm algo()
		{
			return algo;
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> producedMatching()
		{
			return result;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof Result)) return false;
			Result result1 = (Result) o;
			return algo.equals(result1.algo) &&
				result.equals(result1.result);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(algo, result);
		}
	}

	class BepSys implements GroupProjectAlgorithm
	{
		@Override
		public String name()
		{
			// TODO include Pref agg method
			return "BepSys (OG)";
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var groups = new BepSysImprovedGroups(datasetContext.allAgents(), datasetContext.groupSizeConstraint(), true);
			var groupsToProjects = new GroupProjectMaxFlow(datasetContext, groups.asFormedGroups(), datasetContext.allProjects());

			return groupsToProjects;
		}

		@Override
		public String toString()
		{
			return name();
		}
	}

	class BepSys_reworked implements GroupProjectAlgorithm
	{
		@Override
		public String name()
		{
			// TODO include Pref agg method
			return "BepSys (rew)";
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var groups = new BepSysReworked(datasetContext.allAgents(), datasetContext.groupSizeConstraint());
			var groupsToProjects = new GroupProjectMaxFlow(datasetContext, groups.asFormedGroups(), datasetContext.allProjects());

			return groupsToProjects;
		}

		@Override
		public String toString()
		{
			return name();
		}
	}

	class BepSys_ogGroups_minimizeIndividualDisutility implements GroupProjectAlgorithm
	{
		@Override
		public String name()
		{
			// TODO include Pref agg method
			return "BepSys OG Groups (min indiv disutil)";
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var groups = new BepSysImprovedGroups(datasetContext.allAgents(), datasetContext.groupSizeConstraint(), true);

			var groupsToProjects = new GroupProjectMaxFlow(datasetContext, groups.asFormedGroups(), datasetContext.allProjects(),

				// Cost assignment function: the max rank between the individuals within that group
				(projectPreference, theProject) -> {
					var aggPref = ((AggregatedProfilePreference) projectPreference);
					return aggPref.agentsAggregatedFrom().asCollection().stream()
						.map(Agent::projectPreference)
						.filter(Predicate.not(ProjectPreference::isCompletelyIndifferent))
						.mapToInt(pp -> pp.rankOf(theProject).orElse(0))
						.max().orElseThrow();
				});

			return groupsToProjects;
		}

		@Override
		public String toString()
		{
			return name();
		}
	}

	class BepSys_reworkedGroups_minimizeIndividualDisutility implements GroupProjectAlgorithm
	{
		@Override
		public String name()
		{
			// TODO include Pref agg method
			return "BepSys Reworked Groups (min indiv disutil)";
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var groups = new BepSysReworked(datasetContext.allAgents(), datasetContext.groupSizeConstraint());

			var groupsToProjects = new GroupProjectMaxFlow(datasetContext, groups.asFormedGroups(), datasetContext.allProjects(),

				// Cost assignment function: the max rank between the individuals within that group
				(projectPreference, theProject) -> {
					var aggPref = ((AggregatedProfilePreference) projectPreference);
					return aggPref.agentsAggregatedFrom().asCollection().stream()
						.map(Agent::projectPreference)
//						.filter(Predicate.not(ProjectPreference::isCompletelyIndifferent))
						.mapToInt(pp -> pp.rankOf(theProject).orElse(0))
						.max().orElseThrow(() -> {
							System.out.printf("henk");
							return new RuntimeException();
						});
				});

			return groupsToProjects;
		}

		@Override
		public String toString()
		{
			return name();
		}
	}

	class CombinedPrefs implements GroupProjectAlgorithm
	{
		@Override
		public String name()
		{
			return "Peer and Topic preferences merging";
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var formedGroups = new CombinedPreferencesGreedy(datasetContext).asFormedGroups();
			var matching = new GroupProjectMaxFlow(datasetContext, formedGroups, datasetContext.allProjects());

			return matching;
		}

		@Override
		public String toString()
		{
			return name();
		}
	}

	class ILPPP implements GroupProjectAlgorithm
	{
		@Override
		public String name()
		{
			return "ILPPP";
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			return new ILPPPDeterminedMatching(datasetContext);
		}

		@Override
		public String toString()
		{
			return name();
		}
	}

	class RSD implements GroupProjectAlgorithm
	{
		@Override
		public String name()
		{
			return "BepSys groups into Randomised Serial Dictatorship (IA with lottery)";
		}

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var formedGroups = new BepSysImprovedGroups(datasetContext.allAgents(), datasetContext.groupSizeConstraint(), true).asFormedGroups();
			var matching = new RandomizedSerialDictatorship(datasetContext, formedGroups, datasetContext.allProjects());

			return matching;
		}

		@Override
		public String toString()
		{
			return name();
		}
	}

	class Pessimism implements GroupProjectAlgorithm
	{
		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			WorstHumblePairingsSearch p = new WorstHumblePairingsSearch(datasetContext.allAgents(), datasetContext.allProjects(), datasetContext.groupSizeConstraint());
			var agentsToProjects = p.matching();

			return FormedGroupToProjectMatching.from(agentsToProjects);
		}

		@Override
		public String name()
		{
			return "MinMax BnB 'Pessimism'";
		}
	}

	class SDPCWithSlots implements GroupProjectAlgorithm
	{

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var sdpc = new SerialDictatorshipWithProjClosures(datasetContext.allAgents(), datasetContext.allProjects(), datasetContext.groupSizeConstraint());
			var matchingStudentsToProjects = sdpc.doIt();

			return FormedGroupToProjectMatching.from(matchingStudentsToProjects);
		}

		@Override
		public String name()
		{
			return "SDPC-S (project slots)";
		}
	}

	class SDPCWithSlots_potential_numgroupmates_ordered implements GroupProjectAlgorithm
	{

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var sdpc = new SDPCOrderedByPotentialGroupmates(datasetContext.allAgents(), datasetContext.allProjects(), datasetContext.groupSizeConstraint());
			var matchingStudentsToProjects = sdpc.doIt();

			return FormedGroupToProjectMatching.from(matchingStudentsToProjects);
		}

		@Override
		public String name()
		{
			return "SDPC-S (ordered by num potential groupmates) ";
		}
	}

	class Greedy_SDPC_Pessimism_inspired implements GroupProjectAlgorithm
	{

		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var sdpc = new nl.tudelft.aidm.optimalgroups.algorithm.holistic.spdc.SDPCPessimism(datasetContext.allAgents(), datasetContext.allProjects(), datasetContext.groupSizeConstraint());
			var matchingStudentsToProjects = sdpc.doIt();

			return FormedGroupToProjectMatching.from(matchingStudentsToProjects);
		}

		@Override
		public String name()
		{
			return "Greedy (SDPC and Pessimism inspired)";
		}
	}

	class MinizincMIP implements GroupProjectAlgorithm
	{
		@Override
		public GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext)
		{
			var matching = new GroupedProjectMinizincAllocation(datasetContext, 5).matching();

			return FormedGroupToProjectMatching.from(matching);
		}

		@Override
		public String name()
		{
			return "MiniZinc MIP (COIN-BC)";
		}
	}
}
