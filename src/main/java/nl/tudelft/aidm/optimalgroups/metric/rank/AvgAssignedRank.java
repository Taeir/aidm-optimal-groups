package nl.tudelft.aidm.optimalgroups.metric.rank;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.OptionalInt;

public interface AvgAssignedRank
{
	Double asDouble();

	class AssignedProjectToGroup implements AvgAssignedRank
	{
		private final Matching<? extends Group, Project> matching;
		private Double asDouble = null;

		public AssignedProjectToGroup(Matching<? extends Group, Project> matching)
		{
			this.matching = matching;
		}

		public Double asDouble()
		{
			if (asDouble == null) {
				asDouble = calculate();
			}

			return asDouble;
		}

		private Double calculate()
		{
			var sum = matching.asList().stream()
				.map(AssignedRank.ProjectToGroup::new)
				.map(AssignedRank.ProjectToGroup::asInt).flatMapToInt(OptionalInt::stream)
				.sum() * 1.0;

			var numStudents = matching.asList().size();

			return sum / numStudents;
		}
	}

	class AssignedProjectToAgent implements AvgAssignedRank
	{
		private final Matching<Agent, Project> matching;
		private Double asDouble = null;

		public AssignedProjectToAgent(Matching<Agent, Project> matching)
		{
			this.matching = matching;
		}

		public Double asDouble()
		{
			if (asDouble == null) {
				asDouble = calculate();
			}

			return asDouble;
		}

		private Double calculate()
		{
			var sum = matching.asList().stream()
				.map(AssignedRank.ProjectToStudent::new)
				.map(AssignedRank.ProjectToStudent::asInt).flatMapToInt(OptionalInt::stream)
				.sum() * 1.0;

			var numStudents = matching.asList().size();

			return sum / numStudents;
		}
	}
}
