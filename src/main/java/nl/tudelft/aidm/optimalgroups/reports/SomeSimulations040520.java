package nl.tudelft.aidm.optimalgroups.reports;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import net.steppschuh.markdowngenerator.Markdown;
import nl.tudelft.aidm.optimalgroups.algorithm.BepSys_TGAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.CombinedPrefs_TGAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.RSD_TGAlgorithm;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.generated.GeneratedDataContext;
import nl.tudelft.aidm.optimalgroups.dataset.generated.UniformProjectPreferencesGenerator;
import nl.tudelft.aidm.optimalgroups.dataset.generated.NormallyDistributedProjectPreferencesGenerator;
import nl.tudelft.aidm.optimalgroups.dataset.generated.PreferenceGenerator;
import nl.tudelft.aidm.optimalgroups.experiment.Experiment;
import nl.tudelft.aidm.optimalgroups.metric.PopularityMatrix;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import plouchtch.util.Try;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SomeSimulations040520
{
	public static void main(String[] args)
	{
		var algorithms = List.of(
			new BepSys_TGAlgorithm(),
			new CombinedPrefs_TGAlgorithm(),
			new RSD_TGAlgorithm()
			/*new ILPPP_TGAlgorithm()*/); // will not succeed on CE10

		var groupSize = GroupSizeConstraint.manual(4, 5);

		var markdown = new StringBuffer().append(Markdown.heading("Simulation results", 1));
		var expeirmentInMarkdown = new StringBuffer();

		/* CE 10 */
		DatasetContext dataContext = CourseEdition.fromLocalBepSysDbSnapshot(10);

		int numSlots = 5;
		int numProjects = dataContext.allProjects().count();
		int numAgents = dataContext.allAgents().count();

		var projects = dataContext.allProjects();

		var experiment = new Experiment(dataContext, algorithms);
		expeirmentInMarkdown = experimentToMarkdown(groupSize, dataContext, numSlots, numProjects, numAgents, experiment);


//		algorithms = List.of(
//			new BepSys_TGAlgorithm(),
//			new CombinedPrefs_TGAlgorithm(),
//			new ILPPP_TGAlgorithm());

		/* GENERATED DATA  */
		numSlots = 1;
		numProjects = 40;
		numAgents = numProjects * groupSize.maxSize();

		projects = Projects.generated(40, numSlots);
		PreferenceGenerator prefGenerator = new NormallyDistributedProjectPreferencesGenerator(projects, 4);
		dataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		experiment = new Experiment(dataContext, algorithms);
		expeirmentInMarkdown = experimentToMarkdown(groupSize, dataContext, numSlots, numProjects, numAgents, experiment);
		markdown.append(expeirmentInMarkdown);

		/* */
		numSlots = 3;
		numProjects = 40;
		numAgents = numProjects * groupSize.maxSize();

		projects = Projects.generated(40, numSlots);
		prefGenerator = new NormallyDistributedProjectPreferencesGenerator(projects, 4);
		dataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		experiment = new Experiment(dataContext, algorithms);
		expeirmentInMarkdown = experimentToMarkdown(groupSize, dataContext, numSlots, numProjects, numAgents, experiment);
		markdown.append(expeirmentInMarkdown);

		/* */
		numSlots = 3;
		numProjects = 40;
		numAgents = numProjects * groupSize.maxSize();

		projects = Projects.generated(40, numSlots);
		prefGenerator = new NormallyDistributedProjectPreferencesGenerator(projects, 16);
		dataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		experiment = new Experiment(dataContext, algorithms);
		expeirmentInMarkdown = experimentToMarkdown(groupSize, dataContext, numSlots, numProjects, numAgents, experiment);
		markdown.append(expeirmentInMarkdown);

		/* */
		numSlots = 3;
		numProjects = 40;
		numAgents = numProjects * groupSize.maxSize();

		projects = Projects.generated(40, numSlots);
		prefGenerator = new UniformProjectPreferencesGenerator(projects);
		dataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		experiment = new Experiment(dataContext, algorithms);
		expeirmentInMarkdown = experimentToMarkdown(groupSize, dataContext, numSlots, numProjects, numAgents, experiment);
		markdown.append(expeirmentInMarkdown);

//		/* */
//		numSlots = 3;
//		numProjects = 40;
//		numAgents = numProjects * groupSize.maxSize();
//
//		projects = Projects.generated(40, numSlots);
//		prefGenerator = new UniformProjectPreferencesGenerator(projects, 1);
//		generatedDataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);
//
//		experiment = new Experiment(generatedDataContext, algorithms);
//
//		doc += Markdown.heading("Experiment - " + generatedDataContext.identifier()).toString() + "\n";
//		doc += datasetInfo(numAgents, numProjects, numSlots, groupSize);
//		doc += algoResultsInMarkdown(experiment.result().results);
//		doc += popularityInMarkdown(experiment.result().popularityMatrix);
//		doc += Markdown.rule();
//
//
//		/* */
//		numSlots = 3;
//		numProjects = 40;
//		numAgents = numProjects * groupSize.maxSize();
//
//		projects = Projects.generated(40, numSlots);
//		prefGenerator = new UniformProjectPreferencesGenerator(projects, 2);
//		generatedDataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);
//
//		experiment = new Experiment(generatedDataContext, algorithms);
//
//		doc += Markdown.heading("Experiment - " + generatedDataContext.identifier()).toString() + "\n";
//		doc += datasetInfo(numAgents, numProjects, numSlots, groupSize);
//		doc += algoResultsInMarkdown(experiment.result().results);
//		doc += popularityInMarkdown(experiment.result().popularityMatrix);
//		doc += Markdown.rule();

//		var markdownAsString = markdown.toString();
		toPdf(markdown);
		return;
	}

	private static StringBuffer experimentToMarkdown(GroupSizeConstraint groupSize, DatasetContext dataContext, int numSlots, int numProjects, int numAgents, Experiment experiment)
	{
		var doc = new StringBuffer();

		doc.append(Markdown.heading("Experiment - " + dataContext.identifier(), 2).toString()).append("\n");
		doc.append(datasetInfo(experiment, numAgents, numProjects, numSlots, groupSize));
		doc.append(algoResultsInMarkdown(experiment.result().results));
		doc.append(popularityInMarkdown(experiment.result().popularityMatrix));
		doc.append(Markdown.rule()).append("\n");

		return doc;
	}

	static StringBuffer datasetInfo(Experiment experiment, int numAgents, int numProjects, int numSlots, GroupSizeConstraint groupSize)
	{
		var doc = new StringBuffer();
		doc.append(Markdown.heading("Dataset info", 3).toString()).append("\n");
		doc.append(Markdown.image(embed(experiment.projectRankingDistribution.asChart()))).append("\n");
		doc.append(Markdown.unorderedList(
			"\\#agents: " + numAgents,
			"\\#projects: " + numProjects,
			"\\#slots per project: " + numSlots,
			"group sizes, min: " + groupSize.minSize() + ", max: " + groupSize.maxSize()
		)).append("\n");

		return doc;
	}

	static StringBuffer algoResultsInMarkdown(List<Experiment.ExperimentAlgorithmResult> algoResults)
	{
		var doc = new StringBuffer();
		for (Experiment.ExperimentAlgorithmResult algoResult : algoResults)
		{
			doc.append(algoResultInMarkdown(algoResult));
		}

		return doc;
	}

	static StringBuffer algoResultInMarkdown(Experiment.ExperimentAlgorithmResult algoResult)
	{
		var doc = new StringBuffer();

		doc.append(Markdown.heading("Algorithm: " + algoResult.algo.name(), 3).toString()).append("\n");


			doc.append(Markdown.heading("Student perspective", 4)).append("\n");
			doc.append(Markdown.image(embed(algoResult.projectProfileCurveStudents.asChart()))).append("\n");
			doc.append(Markdown.unorderedList(
				"Gini: " + algoResult.giniStudentRanking.asDouble(),
				"AUPCR: " + algoResult.aupcrStudent.asDouble(),
				"Worst rank: " + algoResult.worstAssignedProjectRankOfStudents.asInt()
			)).append("\n");

			doc.append(Markdown.heading("Group perspective", 4)).append("\n");
//			doc.append(Markdown.image(embed(algoResult.projectProfileCurveGroup.asChart())) + "\n");
			doc.append(Markdown.unorderedList(
				"Gini: " + algoResult.giniGroupAggregateRanking.asDouble(),
				"AUPCR: " + algoResult.aupcrGroup.asDouble()
			)).append("\n");

		return doc;
	}

	static String popularityInMarkdown(PopularityMatrix popularityMatrix)
	{
		String doc = Markdown.heading("Algorithm popularity", 3) + "\n" +
			Markdown.text("A matching is more popular than some other, if more agents prefer it to the other - that is, they are better off.") + "\n" +
			Markdown.unorderedList((Object[]) popularityMatrix.asSet().toArray(Object[]::new)) + "\n";

		return doc;
	}

	static String embed(JFreeChart chart)
	{
		try {
			var data = ChartUtils.encodeAsPNG(chart.createBufferedImage(1000,800));
			return "data:image/png;base64," + new String(Base64.encodeBase64(data));


		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static void toHtml(StringBuffer markdown)
	{
		MutableDataSet options = new MutableDataSet();

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		Document parsed = parser.parse(markdown.toString());
		var html = renderer.render(parsed);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("results/experiments/output.html"));
			writer.write(html);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static void toPdf(StringBuffer markdown)
	{
		MutableDataSet options = new MutableDataSet();

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		Document parsed = parser.parse(markdown.toString());
		var html = htmlWithCss(renderer.render(parsed));

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("results/experiments/output.html"));
			writer.write(html);
			PdfConverterExtension.exportToPdf(new File("results/experiments/bla.pdf").getAbsolutePath(), html, "", options);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static String htmlWithCss(String html)
	{
		try
		{
			var css = new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("markdown.css").readAllBytes(), StandardCharsets.UTF_8);

			return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" +
				"<style type=\"text/css\">" + css + "</style>" +
				"</head><body>" + html + "\n" +
				"</body></html>";
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}
}
