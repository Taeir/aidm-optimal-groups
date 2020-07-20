package nl.tudelft.aidm.optimalgroups.algorithm.holistic.pessimism;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Set;

record ProjectAgentsPairing(Project project, Set<Agent>agents, Set<Agent> possibleGroupmates){}
