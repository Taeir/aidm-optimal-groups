package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.Algorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.project.AgentProjectMaxFlowMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.da.SPDAMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

import java.util.Objects;

public interface AgentProjectAlgorithm extends Algorithm
{
	AgentToProjectMatching determineMatching(DatasetContext datasetContext);

	class Result implements Algorithm.Result<AgentProjectAlgorithm, AgentToProjectMatching>
	{
		private final AgentProjectAlgorithm algo;
		private final AgentToProjectMatching producedMatching;

		public Result(AgentProjectAlgorithm algo, AgentToProjectMatching producedMatching)
		{
			this.algo = algo;
			this.producedMatching = producedMatching;
		}

		@Override
		public Algorithm algo()
		{
			return algo;
		}

		@Override
		public AgentToProjectMatching producedMatching()
		{
			return producedMatching;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof Result)) return false;
			if (!super.equals(o)) return false;
			Result result1 = (Result) o;
			return algo.equals(result1.algo) &&
				producedMatching.equals(result1.producedMatching);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(super.hashCode(), algo, producedMatching);
		}
	}

	class DeferredAcceptance implements AgentProjectAlgorithm
	{
		@Override
		public String name()
		{
			return "DA (SOSM)";
		}

		@Override
		public AgentToProjectMatching determineMatching(DatasetContext datasetContext)
		{
			return new SPDAMatching(datasetContext);
		}
	}

	class MinCostMaxFlow implements AgentProjectAlgorithm
	{
		@Override
		public String name()
		{
			return "Maxflow";
		}

		@Override
		public AgentToProjectMatching determineMatching(DatasetContext datasetContext)
		{
			return new AgentProjectMaxFlowMatching(datasetContext);
		}
	}

	class MinCostMaxFlow_ExpCosts implements AgentProjectAlgorithm
	{
		@Override
		public String name()
		{
			return "Maxflow (exp cost)";
		}

		@Override
		public AgentToProjectMatching determineMatching(DatasetContext datasetContext)
		{
			PreferencesToCostFn preferencesToCostFn = (projectPreference, theProject) ->
			{
				// If project is not in preferences (either indifference or a "do-not-want" project,
				// assign it the highest cost.
				int maxRank = datasetContext.allProjects().count();
				int rank = projectPreference.rankOf(theProject).orElse(maxRank);
				return (int) Math.pow(rank, 2);
			};

			return new AgentProjectMaxFlowMatching(datasetContext, preferencesToCostFn);
		}
	}
}
