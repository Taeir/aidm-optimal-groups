package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.Algorithm;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Objects;

public interface TopicGroupAlgorithm extends Algorithm
{
	GroupToProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext);

	class Result implements Algorithm.Result<TopicGroupAlgorithm, GroupToProjectMatching<Group.FormedGroup>>
	{
		private final TopicGroupAlgorithm algo;
		private final GroupToProjectMatching<Group.FormedGroup> result;

		public Result(TopicGroupAlgorithm algo, GroupToProjectMatching<Group.FormedGroup> result)
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
		public GroupToProjectMatching<Group.FormedGroup> result()
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
}
