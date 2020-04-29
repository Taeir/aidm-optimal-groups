package nl.tudelft.aidm.optimalgroups.model.dataset;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.support.ImplementMe;

public class RelabledCourseEditionContext implements DatasetContext
{
	@Override
	public String identifier()
	{
		throw new ImplementMe();
	}

	@Override
	public Projects allProjects()
	{
		throw new ImplementMe();
	}

	@Override
	public Agents allAgents()
	{
		throw new ImplementMe();
	}

	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		throw new ImplementMe();
	}
}
