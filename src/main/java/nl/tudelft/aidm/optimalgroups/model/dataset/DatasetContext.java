package nl.tudelft.aidm.optimalgroups.model.dataset;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.OptionalInt;

/**
 * The instance dataset. Each project and agent lives within this context.
 * @param <PROJECTS> Allows subtypes to express a subtype of the projects impl - keep raw if not needed
 * @param <AGENTS> Allows subtypes to express a subtype of the agents impl - keep raw if not needed
 */
public interface DatasetContext<PROJECTS extends Projects, AGENTS extends Agents>
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
	PROJECTS allProjects();

	/**
	 * All the agents that are part of the dataset
	 * @return Agents in dataset
	 */
	AGENTS allAgents();

	GroupSizeConstraint groupSizeConstraint();

	default int numMaxSlots()
	{
		return this.allProjects().asCollection().stream().map(proj -> proj.slots().size()).max(Integer::compareTo)
			.orElseThrow(() -> new RuntimeException(String.format("None of the projects in dataset [%s] have slots. Bug?", this)));
	}
	
	/**
	 * Returns worst rank among all agents. The rank is less than allProjects().count() if none of the agents find all projects acceptable
	 * and/or there are ties present.
	 * @return The worst rank, empty is everyone is indifferent
	 */
	default OptionalInt worstRank()
	{
		return this.allAgents().asCollection().stream()
			.flatMapToInt(agent -> agent.projectPreference().maxRank().stream())
			.max();
	}
}
