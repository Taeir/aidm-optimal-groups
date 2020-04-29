package nl.tudelft.aidm.optimalgroups.model.dataset;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public interface DatasetContext
{
	/**
	 * Some kind of Dataset identifier - exposed for serialization / reporting purposes
	 * @return The dataset identifier
	 */
	String identifier();

	/**
	 * All the projects that are part of the dataset
	 * @return Projects in dataset
	 */
	Projects allProjects();

	/**
	 * All the agents that are part of the dataset
	 * @return Agents in dataset
	 */
	Agents allAgents();

	GroupSizeConstraint groupSizeConstraint();
}
