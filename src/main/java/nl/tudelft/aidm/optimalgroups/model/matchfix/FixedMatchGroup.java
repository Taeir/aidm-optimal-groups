package nl.tudelft.aidm.optimalgroups.model.matchfix;

import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.base.ListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.List;

/**
 * Represents a group of agents that must be matched to some determined project (have their matching "fixed")
 */
public class FixedMatchGroup implements Group
{
	private final Agents members;
	private final ProjectPreference singleProjectPref;
	
	/**
	 * @param members        Agents that must be fixed to project as a group
	 * @param fixedToProject The project to which they must be fixed
	 */
	public FixedMatchGroup(Agents members, Project fixedToProject)
	{
		this.members = members;
		// This group does not need to have preferences, it is going to be matched to a single project
		// but nonetheless, a Group has preferences so simply create a preference containing only the project
		// that need to be fixed.
		this.singleProjectPref = new ListBasedProjectPreferences(this, List.of(fixedToProject));
	}
	
	@Override
	public Agents members()
	{
		return members;
	}
	
	@Override
	public ProjectPreference projectPreference()
	{
		return singleProjectPref;
	}
}
