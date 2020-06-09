package nl.tudelft.aidm.optimalgroups.experiment.variantvakken;

import nl.tudelft.aidm.optimalgroups.algorithm.AgentProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.dataset.variantvakken.generated.VariantvakkenSinglePmf;
import nl.tudelft.aidm.optimalgroups.dataset.variantvakken.real.VariantvakkenData2020;
import nl.tudelft.aidm.optimalgroups.experiment.variantvakken.report.ExperimentReportInHtml;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import plouchtch.assertion.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VariantvakkenReportTrialJuni08
{
	static List<AgentProjectAlgorithm> algorithms = List.of(
		new AgentProjectAlgorithm.MinCostMaxFlow(),
		new AgentProjectAlgorithm.DeferredAcceptance(),
		new AgentProjectAlgorithm.MinCostMaxFlow_ExpCosts()
	);

	private static final File file = new File("SET ME");

	public static void main(String[] args)
	{
		var experimentsForInReport = new ArrayList<Experiment>();

		experimentsForInReport.add(caps_data200_sys100_mm80());
		experimentsForInReport.add(caps_data250_sys100_mm80());
		experimentsForInReport.add(caps_data200_sys120_mm80());

		new ExperimentReportInHtml(experimentsForInReport)
			.writeHtmlSourceToFile(new File("reports/VariantvakkenTrialJuni08.html"));

		return;
	}

	static Experiment caps_data200_sys100_mm80()
	{
		var dataset = new VariantvakkenData2020(file, 220, 100, 80);
		assertHasCapacityForAll(dataset);

		var experiment = new Experiment(dataset, algorithms);
		return experiment;
	}

	static Experiment caps_data250_sys100_mm80()
	{
		var dataset = new VariantvakkenData2020(file, 250, 100, 80);
		assertHasCapacityForAll(dataset);

		var experiment = new Experiment(dataset, algorithms);
		return experiment;
	}

	static Experiment caps_data200_sys120_mm80()
	{
		var dataset = new VariantvakkenData2020(file, 200, 120, 80);
		assertHasCapacityForAll(dataset);

		var experiment = new Experiment(dataset, algorithms);
		return experiment;
	}

	private static void assertHasCapacityForAll(DatasetContext dataset)
	{
		int numStudents = dataset.allAgents().asCollection().size();
		int capacity = dataset.allProjects().countAllSlots();

		Assert.that(numStudents <= capacity)
			.orThrowMessage(String.format("Not enough capacity to match all students, have: %s, need: %s", capacity, numStudents));
	}
}
