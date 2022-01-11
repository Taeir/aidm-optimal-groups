package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.Constraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.FixMatchingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MILP_Mechanism_BasicPregrouping
{
	private final DatasetContext<?, ?> datasetContext;
	private final ObjectiveFunction objectiveFunction;
	
	private final Pregrouping pregrouping;
	
	private final Constraint[] matchFixes;
	
	public MILP_Mechanism_BasicPregrouping(DatasetContext<?, ?> datasetContext, ObjectiveFunction objectiveFunction, PregroupingType pregroupingType, Constraint... matchFixes)
	{
		this.datasetContext = datasetContext;
		this.objectiveFunction = objectiveFunction;
		
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
		
		this.matchFixes = matchFixes;
	}
	
	public GroupToProjectMatching<Group.FormedGroup> doIt()
	{
		var constraints = new ArrayList<Constraint>();
		constraints.add(pregrouping.constraint());
		constraints.addAll(Arrays.asList(matchFixes));
		
		return new ChiarandiniBaseModel(datasetContext, objectiveFunction, constraints.toArray(Constraint[]::new)).doIt();
	}
}
