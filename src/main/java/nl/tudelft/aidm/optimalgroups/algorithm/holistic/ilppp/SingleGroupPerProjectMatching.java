package nl.tudelft.aidm.optimalgroups.algorithm.holistic.ilppp;

import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatch;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.algorithm.project.StudentProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.matching.Match;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreferenceOfAgents;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts *all* students matched to some project into a single group
 */
public class SingleGroupPerProjectMatching implements GroupToProjectMatching<Group>
{
	private final AgentToProjectMatching agentToProjectMatching;

	public SingleGroupPerProjectMatching(AgentToProjectMatching agentToProjectMatching)
	{
		this.agentToProjectMatching = agentToProjectMatching;
	}

	private List<Match<Group, Project>> result = null;

	@Override
	public List<Match<Group, Project>> asList()
	{
		if (result != null)
			return result;

		ArrayList<Match<Group, Project>> result = new ArrayList<>();

		FormedGroups formedGroups = new FormedGroups();

		agentToProjectMatching.groupedByProject().forEach((project, agentsAsList) -> {
			Agents agents = Agents.from(agentsAsList);
			Group.TentativeGroup group = new Group.TentativeGroup(agents, ProjectPreferenceOfAgents.aggregateWithGloballyConfiguredAggregationMethod(agents));

			Group.FormedGroup formedGroup = formedGroups.addAsFormed(group);
			result.add(new GroupToProjectMatch<>(formedGroup, project));
		});

		this.result = result;
		return result;
	}

	@Override
	public DatasetContext datasetContext()
	{
		return agentToProjectMatching.datasetContext();
	}

	//	public Matchings<Group, Project> result()
//	{
//		if (theMatching != null)
//			return theMatching;
//
//		FormedGroups formedGroups = new FormedGroups();
//
//		var resultingMatching = new Matchings.ListBasedMatchings<Group, Project>();
//		for (var x : groupedByProject().entrySet())
//		{
//			// TODO: proper group creation
//			Agents agents = Agents.from(x.getValue());
//			Group.TentativeGroup tentativeGroup = new Group.TentativeGroup(agents, new AverageProjectPreferenceOfAgents(agents));
//			Group.FormedGroup formedGroup = formedGroups.addAsFormed(tentativeGroup);
//
//
//			var match = new Matchings.StudentsToProjectMatch(formedGroup, x.getKey());
//			resultingMatching.add(match);
//		}
//
//		theMatching = resultingMatching;
//
//		return theMatching;
//	}
}
