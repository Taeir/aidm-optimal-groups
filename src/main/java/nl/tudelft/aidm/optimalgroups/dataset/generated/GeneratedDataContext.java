package nl.tudelft.aidm.optimalgroups.dataset.generated;

import nl.tudelft.aidm.optimalgroups.dataset.generated.prefs.NormallyDistributedProjectPreferencesGenerator;
import nl.tudelft.aidm.optimalgroups.dataset.generated.prefs.PreferenceGenerator;
import nl.tudelft.aidm.optimalgroups.dataset.generated.prefs.UniformProjectPreferencesGenerator;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeneratedDataContext implements DatasetContext
{
	private final String id;
	private final Agents agents;
	private final Projects projects;
	private final GroupSizeConstraint groupSizeConstraint;

	public GeneratedDataContext(int numAgents, Projects projects, GroupSizeConstraint groupSizeConstraint, PreferenceGenerator generator)
	{
		this.groupSizeConstraint = groupSizeConstraint;

		var hexEpochSeconds = Long.toHexString(Instant.now().getEpochSecond());
		id = String.format("DC[RND_a%s_p%s_%s]_%s", numAgents, projects.count(), hexEpochSeconds, groupSizeConstraint);

		agents = new Agents(this,
			IntStream.rangeClosed(1, numAgents)
				.mapToObj(i -> new Agent.AgentInDatacontext(i, generator.generateNew(), GroupPreference.none(), this))
				.collect(Collectors.toCollection(LinkedHashSet::new))
		);

		this.projects = projects;
	}

	public static GeneratedDataContext withNormallyDistributedProjectPreferences(int numAgents, int numProjects, GroupSizeConstraint groupSizeConstraint, double curveSteepness)
	{
		var projects = Projects.generated(numProjects, 5);
		var generator = new NormallyDistributedProjectPreferencesGenerator(projects, curveSteepness);
		return new GeneratedDataContext(numAgents, projects, groupSizeConstraint, generator);
	}

	public static GeneratedDataContext withUniformlyDistributedProjectPreferences(int numAgents, int numProjects, GroupSizeConstraint groupSizeConstraint, double slopeSteepness)
	{
		var projects = Projects.generated(numProjects, 5);
		var generator = new UniformProjectPreferencesGenerator(projects);
		return new GeneratedDataContext(numAgents, projects, groupSizeConstraint, generator);
	}

	@Override
	public String identifier()
	{
		return id;
	}

	@Override
	public Projects allProjects()
	{
		return projects;
	}

	@Override
	public Agents allAgents()
	{
		return agents;
	}

	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		return groupSizeConstraint;
	}
}
