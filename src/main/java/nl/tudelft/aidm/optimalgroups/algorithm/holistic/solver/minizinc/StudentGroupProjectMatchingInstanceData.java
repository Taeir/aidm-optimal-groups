package nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver.minizinc;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualAgents;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualProjects;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class StudentGroupProjectMatchingInstanceData implements JsonDatafile
{
	private final GroupSizeConstraint groupSizeConstraint;

	private final int topicCapacity;

	private final SequentualAgents agents;
	private final SequentualProjects projects;

	public StudentGroupProjectMatchingInstanceData(SequentualDatasetContext sequentualDatasetContext, int topicCapacity)
	{
		this.groupSizeConstraint = sequentualDatasetContext.groupSizeConstraint();
		this.agents = sequentualDatasetContext.allAgents();
		this.projects = sequentualDatasetContext.allProjects();

		this.topicCapacity = topicCapacity;
	}

	public String asJsonString()
	{
		String json =
				jsonObject(
					keyValue("'min group size'", groupSizeConstraint.minSize()),
					keyValue("'max group size'", groupSizeConstraint.maxSize()),
					keyValue("'topic capacity'", topicCapacity),

					keyValue("'#students'", agents.count()),
					keyValue("'#topics'", projects.count()),

					"\"STUDENT_TOPIC_PREF\": " + preferencesProfile(agents)
				);

		return json;
	}

	/*
		Manual serialization is good enough for now, switch to using Gson later if needed
	*/

	private static String jsonObject(String... data)
	{
		return "{ \n" + String.join(",", data) + "\n }";
	}

	private static String keyValue(String key, String value)
	{
		return '"' + key + '"' + ':' + '"' + value + '"' + '\n';
	}

	private static String keyValue(String key, Integer value)
	{
		return '"' + key + '"' + ':' + value + '\n';
	}

	private static String preferencesProfile(Agents agents)
	{
		var profileAsString = agents.asCollection().stream().sorted(Comparator.comparing(agent -> agent.id))
			.map(agent -> agent.projectPreference().asArray())
			.map(Arrays::toString)
			.collect(Collectors.joining(",\n", "[", "]"));

		return profileAsString;
	}
}
