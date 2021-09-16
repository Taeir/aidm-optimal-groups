package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.ConditionalGroupConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.HardGroupingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.SoftGroupConstraint;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface PregroupingType
{
	String simpleName();
	Pregrouping instantiateFor(DatasetContext datasetContext);
	
	/* Factory Methods */
	
	// ANY CLIQUE
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
	
	/// SIZED
	static PregroupingType sizedCliqueHardGrouped(Integer... sizes)
	{
		var sizesSetNotation = Arrays.stream(sizes).map(Object::toString).collect(Collectors.joining(",", "{", "}"));
		return new NamedLambda(
				"sizedCliques_hardGrp" + sizesSetNotation,
				(datasetContext) -> new Pregrouping.sizedClique(datasetContext, HardGroupingConstraint::new, sizes)
		);
	}
	
	static PregroupingType sizedCliqueSoftGrouped(Integer... sizes)
	{
		var sizesSetNotation = Arrays.stream(sizes).map(Object::toString).collect(Collectors.joining(",", "{", "}"));
		return new NamedLambda(
				"sizedCliques_softGrp" + sizesSetNotation,
				(datasetContext) -> new Pregrouping.sizedClique(datasetContext, SoftGroupConstraint::new, sizes)
		);
	}
	
	static PregroupingType sizedCliqueConditionallyGrouped(int upToIncludingRank, Integer... sizes)
	{
		var sizesSetNotation = Arrays.stream(sizes).map(Object::toString).collect(Collectors.joining(",", "{", "}"));
		return new NamedLambda(
				"sizedCliques_condGrp" + sizesSetNotation,
				(datasetContext) -> new Pregrouping.sizedClique(datasetContext,  groups -> new ConditionalGroupConstraint(groups, upToIncludingRank), sizes)
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
