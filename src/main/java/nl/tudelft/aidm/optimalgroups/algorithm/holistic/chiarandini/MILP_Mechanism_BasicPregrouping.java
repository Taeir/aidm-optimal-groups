package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.ObjectiveFunction;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;

public class MILP_Mechanism_BasicPregrouping
{
	
	private final DatasetContext datasetContext;
	private final ObjectiveFunction objectiveFunction;
	
	private final Pregrouping pregrouping;
	
	public MILP_Mechanism_BasicPregrouping(DatasetContext datasetContext, ObjectiveFunction objectiveFunction, PregroupingType pregroupingType)
	{
		this.datasetContext = datasetContext;
		this.objectiveFunction = objectiveFunction;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
	}
	
	public AgentToProjectMatching doIt()
	{
		return new ChiarandiniBaseModel(datasetContext, objectiveFunction, pregrouping.constraint()).doIt();
	}
	
}
