package nl.tudelft.aidm.optimalgroups.algorithm;

import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

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
	}
}
