package nl.tudelft.aidm.optimalgroups.dataset.bepsys;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;

import java.util.List;
import java.util.stream.Collectors;

public class CourseEditionModNoPeerPref extends CourseEdition
{
	public CourseEditionModNoPeerPref(CourseEdition courseEdition)
	{
		// TODO: pay debts
		super(courseEdition.dataSource, courseEdition.courseEditionId);

		// remove peer prefs from agents
		agents = agents.asCollection().stream()
			.map(agent -> {
				var modAgent = new Agent.AgentInDatacontext(agent.id, agent.projectPreference(), GroupPreference.none(), this);
				return modAgent;
			})
			.collect(Collectors.collectingAndThen(Collectors.toList(),
				(List<Agent> asList) -> Agents.from(asList))
			);
	}

	@Override
	public String identifier()
	{
		return super.identifier() + "[mod-noPeer]";
	}


}
