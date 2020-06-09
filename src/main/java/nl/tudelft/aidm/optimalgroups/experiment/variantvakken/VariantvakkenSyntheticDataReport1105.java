package nl.tudelft.aidm.optimalgroups.experiment.variantvakken;

import nl.tudelft.aidm.optimalgroups.algorithm.AgentProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.dataset.variantvakken.generated.VariantvakkenSinglePmf;
import nl.tudelft.aidm.optimalgroups.experiment.variantvakken.report.ExperimentReportInHtml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VariantvakkenSyntheticDataReport1105
{
	static List<AgentProjectAlgorithm> algorithms = List.of(
		new AgentProjectAlgorithm.MinCostMaxFlow(),
		new AgentProjectAlgorithm.DeferredAcceptance(),
		new AgentProjectAlgorithm.MinCostMaxFlow_ExpCosts()
	);

	public static void main(String[] args)
	{
		var experimentsForInReport = new ArrayList<Experiment>();

		experimentsForInReport.add(capacityBasedPopularityExperiment());
		experimentsForInReport.add(emph220Experiment());
		experimentsForInReport.add(emph110Experiment());
		experimentsForInReport.add(emph88Experiment());
		experimentsForInReport.add(flipPopExperiment());
		experimentsForInReport.add(flipPopExperiment());
		experimentsForInReport.add(flipPopExperiment());

		new ExperimentReportInHtml(experimentsForInReport)
			.writeHtmlSourceToFile(new File("reports/Variantvakken1505.html"));

		return;
	}

	static Experiment capacityBasedPopularityExperiment()
	{
		var dataset = new VariantvakkenSinglePmf(220.0, 110.0, 88.0);

		var experiment = new Experiment(dataset, algorithms);
		return experiment;
	}

	static Experiment emph220Experiment()
	{
		var dataset = new VariantvakkenSinglePmf(280, 110.0, 88.0);

		var experiment = new Experiment(dataset, algorithms);
		return experiment;
	}

	static Experiment emph110Experiment()
	{
		var dataset = new VariantvakkenSinglePmf(220, 160.0, 88.0);

		var experiment = new Experiment(dataset, algorithms);
		return experiment;
	}

	static Experiment emph88Experiment()
	{
		var dataset = new VariantvakkenSinglePmf(220, 110, 140);

		var experiment = new Experiment(dataset, algorithms);
		return experiment;
	}

	static Experiment flipPopExperiment()
	{
		var dataset = new VariantvakkenSinglePmf(88, 110, 220);

		var experiment = new Experiment(dataset, algorithms);
		return experiment;
	}


}
