package nl.tudelft.aidm.optimalgroups.algorithm.generic.da;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public interface Proposer<PROPOSER, PROPOSED>
{
	Proposal<PROPOSER, PROPOSED> makeNextProposal();

	PROPOSER subject();
}
