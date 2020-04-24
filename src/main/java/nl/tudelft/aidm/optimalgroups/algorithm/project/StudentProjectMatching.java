package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.match.Matching;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.List;
import java.util.Map;

public interface StudentProjectMatching extends Matching<Agent, Project>
{
	Map<Project, List<Agent>> groupedByProject();
}
