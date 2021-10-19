package nl.tudelft.aidm.optimalgroups.dataset.bepsys;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.agent.SimpleAgent;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.function.Predicate;

/**
 * A CourseEdition whose set of agents has been modified
 */
public class FilteredAgentsCourseEdition extends CourseEdition
{
	private final CourseEdition originalCourseEdition;
	
	private final Agents agents;
	
	public FilteredAgentsCourseEdition(CourseEdition original, Predicate<Agent.AgentInBepSysSchemaDb> filter)
	{
		super(original.courseEditionId);
		
		this.agents = original.allAgents().asCollection().stream()
				.map(agent -> (SimpleAgent.AgentInBepSysSchemaDb) agent)
				.filter(filter)
				.map(agent -> new SimpleAgent.AgentInBepSysSchemaDb(agent, this))
				.collect(Agents.collector);
		
		originalCourseEdition = original;
	}
	
	
	@Override
	public Projects allProjects()
	{
		return originalCourseEdition.allProjects();
	}
	
	@Override
	public Agents allAgents()
	{
		return this.agents;
	}
	
	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		return originalCourseEdition.groupSizeConstraint();
	}
}
