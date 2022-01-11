package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.Constraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.MinimizeSumOfExpRanks;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.model.UtilitarianWeightsObjective;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

import java.util.ArrayList;
import java.util.Arrays;

public class Chiarandini_MinSumExpRank
{
	private final DatasetContext<?, ?> datasetContext;
	private final Pregrouping pregrouping;
	private final Constraint[] fixes;
	
	private UtilitarianWeightsObjective.WeightScheme weightScheme;

	public Chiarandini_MinSumExpRank(DatasetContext<?, ?> datasetContext, PregroupingType pregroupingType, Constraint... fixes)
	{
		this.datasetContext = datasetContext;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
		this.fixes = fixes;
	}

	public GroupToProjectMatching<Group.FormedGroup> doIt()
	{
		var objFn = new MinimizeSumOfExpRanks();
		var constraints = new ArrayList<Constraint>();
		constraints.add(pregrouping.constraint());
		constraints.addAll(Arrays.asList(fixes));
		
		return new ChiarandiniBaseModel(datasetContext, objFn, constraints.toArray(new Constraint[0])).doIt();
	}
}
