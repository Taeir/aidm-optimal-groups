package nl.tudelft.aidm.optimalgroups.experiment.dataset;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.matchfix.MatchFixes;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import plouchtch.assertion.Assert;

public class ResearchProject2021Q4Dataset extends CourseEdition
{
	private static final int courseEditionId = 39;
	private final String stringIdentifier;
	private final Projects projects;
	private final Agents agentsFiltered;
	private final GroupSizeConstraint groupSizeConstraint;
	
	/**
	 * Creates a processed instance of the RP 2021 Q4 dataset
	 * @return The filtered dataset
	 */
	public static ResearchProject2021Q4Dataset getInstance()
	{
		var dataset = CourseEditionFromDb.fromLocalBepSysDbSnapshot(courseEditionId);
		
		var agentsFiltered = filterAgents(dataset);
		var projects = dataset.allProjects();
		
		var updatedIdentifier = dataset.identifier().replaceAll("\\[s\\d+", "[s" + agentsFiltered.count());
		
		return new ResearchProject2021Q4Dataset(updatedIdentifier + "_processed", projects, agentsFiltered, dataset.groupSizeConstraint());
	}
	
	/**
	 * Private constructor, simply passes the params to the ManualDatasetContext constructor which then simply sets the fields
	 */
	private ResearchProject2021Q4Dataset(String stringIdentifier, Projects projects, Agents agentsFiltered, GroupSizeConstraint groupSizeConstraint)
	{
		super(courseEditionId);
		
		this.stringIdentifier = stringIdentifier;
		this.projects = projects;
		this.agentsFiltered = agentsFiltered;
		this.groupSizeConstraint = groupSizeConstraint;
	}
	
	@Override
	public String identifier()
	{
		return stringIdentifier;
	}
	
	@Override
	public String toString()
	{
		return stringIdentifier;
	}
	
	@Override
	public Projects allProjects()
	{
		return projects;
	}
	
	@Override
	public Agents allAgents()
	{
		return agentsFiltered;
	}
	
	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		return groupSizeConstraint;
	}
	
	/**
	 * @return The matches that need to be "fixed" - a list of predetermined matching outcomes
	 */
	public MatchFixes matchesToFix()
	{
		return RP21Q4Aux.matchesToFix(this);
	}
	
	/**
	 * @return the course edition id this dataset has in bepsys
	 */
	public int courseEditionId()
	{
		return courseEditionId;
	}
	
	private static Agents filterAgents(CourseEdition dataset)
	{
		Assert.that(dataset.bepSysId() == 39).orThrowMessage("Dataset must be for course edition 39");
		
		var agentsToFilterAsIdList = RP21Q4Aux.agentsToFilterOut();
		
		return dataset.allAgents().asCollection().stream()
				.map(agent -> (Agent.AgentInBepSysSchemaDb) agent)
				.filter(agent -> !agentsToFilterAsIdList.contains(agent.bepSysUserId))
				.collect(Agents.collector);
	}
}
