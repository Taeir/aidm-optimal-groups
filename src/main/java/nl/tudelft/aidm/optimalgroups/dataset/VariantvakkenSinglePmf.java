package nl.tudelft.aidm.optimalgroups.dataset;

import nl.tudelft.aidm.optimalgroups.dataset.generated.ProjectPreferencesFromPmfGenerator;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VariantvakkenSinglePmf implements DatasetContext
{
	private final Projects projects;
	private final Agents agents;
	private final GroupSizeConstraint groupSizeConstraint;

	private final double pop220;
	private final double pop110;
	private final double pop88;

	public VariantvakkenSinglePmf(double pop220, double pop110, double pop88)
	{
		this.pop220 = pop220;
		this.pop110 = pop110;
		this.pop88 = pop88;
		var proj220 = new Project.ProjectWithStaticSlotAmount(220, 220);
		var proj110 = new Project.ProjectWithStaticSlotAmount(110, 110);
		var proj88 = new Project.ProjectWithStaticSlotAmount(88, 88);

		this.projects = Projects.from(List.of(proj220, proj88, proj110));

		var pmf = List.<Pair<Project, Double>>of(Pair.create(proj220, pop220), Pair.create(proj110, pop110), Pair.create(proj88, pop88));

		var prefGen = new ProjectPreferencesFromPmfGenerator(projects, pmf);

		this.agents = Agents.from(IntStream.rangeClosed(1, 400).mapToObj(id ->
				new Agent.AgentInDatacontext(
					id,
					prefGen.generateNew(),
					GroupPreference.none(),
					this)
			).collect(Collectors.toList())
		);

		groupSizeConstraint = new GroupSizeConstraint.Manual(0, 1);
	}

	@Override
	public String identifier()
	{
		var popTotal = pop220+pop110+pop88;
		return String.format("Variantvakken[ 220(pop %.3f%%, 110(pop %.3f%%), 88(pop %.3f%%) ]", pop220/popTotal, pop110/popTotal, pop88/popTotal);
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
