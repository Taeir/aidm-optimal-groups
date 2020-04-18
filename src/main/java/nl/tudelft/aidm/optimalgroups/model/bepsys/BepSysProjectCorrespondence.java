package nl.tudelft.aidm.optimalgroups.model.bepsys;

import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.Map;

/**
 * Essentially a map between BepSys project identifiers and [1,|projects|]
 * American heritage dictionary, 5th edition: Correspondence - "A similarity, connection, or equivalence."
 */
public class BepSysProjectCorrespondence
{
	private Projects bepsysProjects;
	private Projects correspondingProjects;

	private Map<Project, Project> correspondence;

	public BepSysProjectCorrespondence(Projects bepsysProjects)
	{
		this.bepsysProjects = bepsysProjects;
	}
}
