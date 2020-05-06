package nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver;

import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.*;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.agent.SequentualAgents;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver.minizinc.StudToGroupTopicMatchingInstanceData;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import nl.tudelft.aidm.optimalgroups.model.project.SequentualProjects;
import org.sql2o.GenericDatasource;
import plouchtch.lang.exception.ImplementMe;

import java.util.List;

public class CPDeterminedMatching implements GroupProjectMatching<Group.FormedGroup>
{

	private final String minizincexec = "C:\\Program Files\\MiniZinc IDE (bundled)\\";

	private final CourseEdition courseEdition;
	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;

	public CPDeterminedMatching(CourseEdition courseEdition)
	{
		this.agents = courseEdition.allAgents();
		this.projects = courseEdition.allProjects();
		this.groupSizeConstraint = courseEdition.groupSizeConstraint();
		this.courseEdition = courseEdition;
	}

	public void doIt()
	{
		var seqProjects = SequentualProjects.from(projects);
		var seqAgents = SequentualAgents.from(agents, seqProjects);

		SequentualAgents.from(agents, seqProjects);
		var instanceData = new StudToGroupTopicMatchingInstanceData(groupSizeConstraint, seqAgents, seqProjects, 5);

		// interface with Minizinc

		return;
	}

	@Override
	public DatasetContext datasetContext()
	{
		return courseEdition;
	}

	@Override
	public List<Match<Group.FormedGroup, Project>> asList()
	{
		throw new ImplementMe();
	}


	// for testing
	public static void main(String[] args)
	{
		var dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/aidm", "henk", "henk");
		CPDeterminedMatching cpDeterminedMatchings = new CPDeterminedMatching(CourseEdition.fromBepSysDatabase(dataSource, 4));

		cpDeterminedMatchings.doIt();
		return;
	}

}
