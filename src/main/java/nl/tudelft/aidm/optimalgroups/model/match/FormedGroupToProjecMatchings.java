package nl.tudelft.aidm.optimalgroups.model.match;

import nl.tudelft.aidm.optimalgroups.algorithm.project.GroupProjectMatchings;
import nl.tudelft.aidm.optimalgroups.model.Group.FormedGroup;
import nl.tudelft.aidm.optimalgroups.model.Project;

import java.util.List;

public class FormedGroupToProjecMatchings extends ListBasedMatchings<FormedGroup, Project> implements GroupProjectMatchings<FormedGroup>
{
	public FormedGroupToProjecMatchings(List<? extends Match<FormedGroup, Project>> list)
	{
		super((List<Match<FormedGroup, Project>>) list);
	}
}
