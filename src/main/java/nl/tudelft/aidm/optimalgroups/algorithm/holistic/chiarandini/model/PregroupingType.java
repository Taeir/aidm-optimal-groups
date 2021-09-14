package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.ConditionalGroupConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.HardGroupingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.SoftGroupConstraint;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;

import java.util.function.Function;

public interface PregroupingType
{
	String simpleName();
	Pregrouping instantiateFor(DatasetContext datasetContext);
	
	/* Factory Methods */
	static PregroupingType anyCliqueHardGrouped()
	{
		return new NamedLambda(
				"anyClique_hardGrp",
				(datasetContext) -> new Pregrouping.anyClique(datasetContext, HardGroupingConstraint::new)
		);
	}
	
	static PregroupingType anyCliqueSoftGrouped()
	{
		return new NamedLambda(
				"anyClique_softGrp",
				(datasetContext) -> new Pregrouping.anyClique(datasetContext, SoftGroupConstraint::new)
		);
	}
	
	static PregroupingType anyCliqueConditionallyGrouped(int upToIncludingRank)
	{
		return new NamedLambda(
				"anyClique_condGrp" + upToIncludingRank,
				(datasetContext) -> new Pregrouping.anyClique(datasetContext, groups -> new ConditionalGroupConstraint(groups, upToIncludingRank))
		);
	}
	
	/* */
	record NamedLambda(String simpleName, Function<DatasetContext, Pregrouping> function) implements PregroupingType
	{
		@Override
		public Pregrouping instantiateFor(DatasetContext datasetContext)
		{
			return function.apply(datasetContext);
		}
	}
	
}
