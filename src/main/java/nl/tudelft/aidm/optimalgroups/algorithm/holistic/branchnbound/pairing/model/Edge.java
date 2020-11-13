package nl.tudelft.aidm.optimalgroups.algorithm.holistic.branchnbound.pairing.model;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

public record Edge(Agent from, Project to, int rank)
{

}