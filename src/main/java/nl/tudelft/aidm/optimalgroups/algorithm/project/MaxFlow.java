package nl.tudelft.aidm.optimalgroups.algorithm.project;

import edu.princeton.cs.algs4.BipartiteMatching;
import nl.tudelft.aidm.optimalgroups.model.entity.Groups;
import nl.tudelft.aidm.optimalgroups.support.ImplementMe;

public class MaxFlow implements ProjectMatchingAlgorithm
{
	@Override
	public Matching doMatching(Groups groups)
	{
		throw new ImplementMe();


		BipartiteMatching bipartiteMatching = new BipartiteMatching();

	}

	/**
	 * Course edition has projects,
	 * Students have preferences over the projects for a course editon
	 *
	 * Each project can have 'max_number_of_groups' groups
	 *
	 *
	 * left: array of group id's
	 * right: project id's (note: projects have spots for multiple groups!)
	 *
	 * determine group preference for each group
	 *
	 * create edges between groups and projects with weight the priority (smaller numbers are higher prio)
	 *     if preference is 0, use a very high weight
	 *
	 * run GraphMatch with minimize cost
	 *
	 */
}
