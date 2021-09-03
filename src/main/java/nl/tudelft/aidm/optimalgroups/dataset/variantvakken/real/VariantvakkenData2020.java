package nl.tudelft.aidm.optimalgroups.dataset.variantvakken.real;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.GroupPreference;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.ListBasedProjects;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.apache.commons.lang3.StringUtils;
import plouchtch.assertion.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class VariantvakkenData2020 implements DatasetContext
{
	private final File data;
	private final int capData;
	private final int capSystems;
	private final int capMultimedia;

	private final Variantvakken variantvakken;
	private final Agents studenten;

	// each slot has at most 1 student assigned to it
	private final GroupSizeConstraint gsc = new GroupSizeConstraint.Manual(0, 1);

	public VariantvakkenData2020(File data, int capData, int capSystems, int capMultimedia)
	{
		this.data = data;
		this.capData = capData;
		this.capSystems = capSystems;
		this.capMultimedia = capMultimedia;

		variantvakken = new Variantvakken(capData, capSystems, capMultimedia);
		studenten = loadAgentsFromFile();
	}

	@Override
	public String identifier()
	{
		return String.format("Variantvakken 2020 - July 08 (Data: %s, Sys: %s, MM: %s)", capData, capSystems, capMultimedia);
	}

	@Override
	public Variantvakken allProjects()
	{
		return variantvakken;
	}

	@Override
	public Agents allAgents()
	{
		return studenten;
	}

	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		return gsc;
	}

	private Agents loadAgentsFromFile()
	{
		var studentToPrefsMap = new HashMap<Long, VariantvakPreference>();

		try(var lines = Files.lines(data.toPath()))
		{
			// skip header
			lines.skip(1).forEach(line -> {
				String[] cols = StringUtils.split(line, ',');

				Long student = Long.parseLong(cols[0]);
				String col = cols[1];
				Variantvak variantvak = allProjects().findByName(col);
				Integer rank = Integer.parseInt(cols[2]);

				var prefOfStudent = studentToPrefsMap.computeIfAbsent(student, __ -> new VariantvakPreference(student));
				prefOfStudent.include(variantvak, rank);
			});

			var listOfAgents = studentToPrefsMap.entrySet().stream()
				.map(entry ->
				{
					if (entry.getKey() >= Integer.MAX_VALUE)
						throw new RuntimeException("Student Id larger than an int :(");

					var id = entry.getKey().intValue();
					var pref = entry.getValue();

					return (Agent) new Agent.AgentInDatacontext(id, pref, GroupPreference.none(), this);
				})
				.collect(Collectors.toList());

			return Agents.from(listOfAgents);

		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private static class Variantvakken extends ListBasedProjects
	{
		private final List<Variantvak> asList;

		private final Variantvak data;
		private final Variantvak sys;
		private final Variantvak mm;

		public Variantvakken(int capData, int capSystems, int capMultimedia)
		{
			this.asList = List.of(
				data = new Variantvak("Data", 1, capData),
				sys = new Variantvak("Systems", 2, capSystems),
				mm = new Variantvak("Multimedia", 3, capMultimedia)
			);
		}

		@Override
		protected List<Project> projectList()
		{
			return Collections.unmodifiableList(asList);
		}

		public Variantvak findByName(String name)
		{
			return switch (name)
				{
					case "Data" -> data;
					case "Systems" -> sys;
					case "Multimedia" -> mm;
					default -> throw new RuntimeException("Cannot map '" + name + "' variant vak - mapping not found");
				};
		}
	}

	private static class Variantvak extends Project.ProjectWithStaticSlotAmount
	{
		private final String name;

		public Variantvak(String name, int id, int capacity)
		{
			super(id, capacity);
			this.name = name;
		}

		@Override
		public String name()
		{
			return name;
		}
	}

	private static class VariantvakPreference implements ProjectPreference
	{
		private final Project[] asListOfProjects;
		private final Object owner;

		public VariantvakPreference(Object owner)
		{
			this.owner = owner;
			asListOfProjects = new Project[3];
		}

		public void include(Project project, int rank)
		{
			Assert.that(1 <= rank && rank <= 3).orThrowMessage("Rank < 1 or 3 < Rank, was: " + rank);

			asListOfProjects[rank - 1] = project;
		}

		@Override
		public Object owner()
		{
			return owner;
		}

		@Override
		public Integer[] asArray()
		{
			return Arrays.stream(asListOfProjects)
				.map(Project::id)
				.toArray(Integer[]::new);
		}

		@Override
		public List<Project> asList()
		{
			return List.of(asListOfProjects);
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			VariantvakPreference that = (VariantvakPreference) o;
			return Arrays.equals(asListOfProjects, that.asListOfProjects);
		}

		@Override
		public int hashCode()
		{
			return Arrays.hashCode(asListOfProjects);
		}
	}
}
