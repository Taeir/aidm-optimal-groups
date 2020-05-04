package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Objects;

public interface TopicGroupAlgorithm
{
	String name();

	GroupProjectMatching<Group.FormedGroup> determineMatching(DatasetContext datasetContext);

	class Result
	{
		public final TopicGroupAlgorithm algo;
		public final Matching<Group.FormedGroup, Project> result;

		public Result(TopicGroupAlgorithm algo, Matching<Group.FormedGroup, Project> result)
		{
			this.algo = algo;
			this.result = result;
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
