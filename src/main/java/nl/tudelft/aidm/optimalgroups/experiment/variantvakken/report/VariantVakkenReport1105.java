package nl.tudelft.aidm.optimalgroups.experiment.variantvakken.report;

import nl.tudelft.aidm.optimalgroups.algorithm.DeferredAccpetanceSPAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.MaxFlowSPAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.StudentProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.dataset.VariantvakkenSinglePmf;
import nl.tudelft.aidm.optimalgroups.experiment.variantvakken.Experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VariantVakkenReport1105
{

	static List<StudentProjectAlgorithm> algorithms = List.of(
		new MaxFlowSPAlgorithm(),
		new DeferredAccpetanceSPAlgorithm()
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
			.writeHtmlSourceToFile(new File("reports/Variantvakken1105.html"));

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
