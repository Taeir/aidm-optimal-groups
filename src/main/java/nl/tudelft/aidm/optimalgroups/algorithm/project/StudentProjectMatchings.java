package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.match.Matchings;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.Project;

import java.util.List;
import java.util.Map;

public interface StudentProjectMatchings extends Matchings<Agent, Project>
{
	Map<Project, List<Agent>> groupedByProject();
}
