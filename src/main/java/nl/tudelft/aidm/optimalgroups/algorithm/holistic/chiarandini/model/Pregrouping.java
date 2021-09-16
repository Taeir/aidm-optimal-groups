package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.partial.CliqueGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.Constraint;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;

import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public interface Pregrouping
{
	Groups<Group.TentativeGroup> groups();
	
	Constraint constraint();
	
	class anyClique implements Pregrouping
	{
		private final Groups<Group.TentativeGroup> groups;
		private final Constraint constraint;
		
		public anyClique(DatasetContext datasetContext, Function<Groups<?>, Constraint> groupingConstraintProvider)
		{
			this.groups = new CliqueGroups(datasetContext.allAgents());
			this.constraint = groupingConstraintProvider.apply(this.groups);
		}
		
		@Override
		public Groups<Group.TentativeGroup> groups()
		{
			return groups;
		}
		
		@Override
		public Constraint constraint()
		{
			return constraint;
		}
	}
	
	class sizedClique implements Pregrouping
	{
		private final Groups<Group.TentativeGroup> groups;
		private final Constraint constraint;
		
		public sizedClique(DatasetContext datasetContext, Function<Groups<?>, Constraint> groupingConstraintProvider, Integer... sizes)
		{
			this.groups = new CliqueGroups(datasetContext.allAgents()).ofSizes(sizes);
			this.constraint = groupingConstraintProvider.apply(groups);
		}
		
		@Override
		public Groups<Group.TentativeGroup> groups()
		{
			return this.groups;
		}
		
		@Override
		public Constraint constraint()
		{
			return this.constraint;
		}
	}
}
