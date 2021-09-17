package nl.tudelft.aidm.optimalgroups.dataset.bepsys;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

public class LocallyModifiedCourseEdition extends CourseEdition
{
	private final CourseEdition originalCourseEdition;
	
	private final Agents agents;
	private final Projects projects;
	
	public LocallyModifiedCourseEdition(CourseEdition courseEdition, Agents agents, Projects projects)
	{
		super(courseEdition.courseEditionId);
		
		this.agents = agents.asCollection().stream()
				.map(agent -> (Agent.AgentInBepSysSchemaDb) agent)
				.map(agent -> new Agent.AgentInBepSysSchemaDb(agent, this))
				.collect(Agents.collector);
		
		this.projects = projects;
		
		originalCourseEdition = courseEdition;
	}
	
	@Override
	public Projects allProjects()
	{
		return projects;
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
