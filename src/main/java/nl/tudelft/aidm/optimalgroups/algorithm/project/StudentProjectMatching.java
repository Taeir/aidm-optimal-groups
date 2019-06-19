package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.entity.Agent;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import nl.tudelft.aidm.optimalgroups.model.entity.Project;

import java.util.List;
import java.util.Map;

public interface StudentProjectMatching extends Matching<Agent, Project>
{
	Map<Project, List<Agent>> groupedByProject();
}
