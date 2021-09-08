package nl.tudelft.aidm.optimalgroups.dataset.bepsys;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.project.ProjectsInDb;
import org.sql2o.GenericDatasource;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import plouchtch.util.ComputerName;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class CourseEdition implements DatasetContext
{
	protected final int courseEditionId;
	
	protected CourseEdition(int courseEditionId)
	{
		this.courseEditionId = courseEditionId;
	}

	public Integer bepSysId()
	{
		return courseEditionId;
	}

	@Override
	public String identifier()
	{
		return String.format("CourseEdition[%s]_[s%s_p%s]_%s", courseEditionId, allAgents().count(), allProjects().count(), groupSizeConstraint());
	}
	
	public Optional<Agent> findAgentByUserId(Integer bepSysUserId)
	{
		return this.allAgents().asCollection().stream()
				.map(agent -> (Agent.AgentInBepSysSchemaDb) agent)
				.filter(agent -> agent.bepSysUserId.equals(bepSysUserId))
				.map(agent -> (Agent) agent)
				.findAny();
	}
	
	public Optional<Project> findProjectByProjectId(Integer bepSysProjectId)
	{
		return this.allProjects().asCollection().stream()
				.map(project -> (Project.BepSysProject) project)
				.filter(project -> bepSysProjectId.equals(project.bepsysId))
				.map(project -> (Project) project)
				.findAny();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CourseEdition that = (CourseEdition) o;
		return identifier().equals(that.identifier());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(identifier());
	}
}
