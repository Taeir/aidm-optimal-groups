package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.Algorithm;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

import java.util.Objects;

public interface StudentProjectAlgorithm extends Algorithm
{
	AgentToProjectMatching determineMatching(DatasetContext datasetContext);

	class Result implements Algorithm.Result<StudentProjectAlgorithm, AgentToProjectMatching>
	{
		private final StudentProjectAlgorithm algo;
		private final AgentToProjectMatching result;

		public Result(StudentProjectAlgorithm algo, AgentToProjectMatching result)
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
		public AgentToProjectMatching result()
		{
			return result;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof Result)) return false;
			if (!super.equals(o)) return false;
			Result result1 = (Result) o;
			return algo.equals(result1.algo) &&
				result.equals(result1.result);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(super.hashCode(), algo, result);
		}
	}
}
