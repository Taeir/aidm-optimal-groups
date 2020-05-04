package nl.tudelft.aidm.optimalgroups.reports;

import net.steppschuh.markdowngenerator.Markdown;
import nl.tudelft.aidm.optimalgroups.algorithm.BepSys_TGAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.CombinedPrefs_TGAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.ILPPP_TGAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.RSD_TGAlgorithm;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.generated.GeneratedDataContext;
import nl.tudelft.aidm.optimalgroups.dataset.generated.UniformProjectPreferencesGenerator;
import nl.tudelft.aidm.optimalgroups.dataset.generated.NormallyDistributedProjectPreferencesGenerator;
import nl.tudelft.aidm.optimalgroups.dataset.generated.PreferenceGenerator;
import nl.tudelft.aidm.optimalgroups.experiment.Experiment;
import nl.tudelft.aidm.optimalgroups.metric.PopularityMatrix;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

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

		var doc = "";

		/* CE 10 */
		var dataContext = CourseEdition.fromLocalBepSysDbSnapshot(10);

		int numSlots = 5;
		int numProjects = dataContext.allProjects().count();
		int numAgents = dataContext.allAgents().count();

		var projects = dataContext.allProjects();

		var experiment = new Experiment(dataContext, algorithms);

		doc += Markdown.heading("Experiment - " + dataContext.identifier()).toString() + "\n";
		doc += datasetInfo(experiment, numAgents, numProjects, numSlots, groupSize);
		doc += algoResultsInMarkdown(experiment.result().results);
		doc += popularityInMarkdown(experiment.result().popularityMatrix);
		doc += Markdown.rule() + "\n";



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
		var generatedDataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);


		experiment = new Experiment(dataContext, algorithms);

		doc += Markdown.heading("Experiment - " + dataContext.identifier()).toString() + "\n";
		doc += datasetInfo(experiment, numAgents, numProjects, numSlots, groupSize);
		doc += algoResultsInMarkdown(experiment.result().results);
		doc += popularityInMarkdown(experiment.result().popularityMatrix);
		doc += Markdown.rule() + "\n";


		/* */
		numSlots = 3;
		numProjects = 40;
		numAgents = numProjects * groupSize.maxSize();

		projects = Projects.generated(40, numSlots);
		prefGenerator = new NormallyDistributedProjectPreferencesGenerator(projects, 4);
		generatedDataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		experiment = new Experiment(generatedDataContext, algorithms);

		doc += Markdown.heading("Experiment - " + generatedDataContext.identifier()).toString() + "\n";
		doc += datasetInfo(experiment, numAgents, numProjects, numSlots, groupSize);
		doc += algoResultsInMarkdown(experiment.result().results);
		doc += popularityInMarkdown(experiment.result().popularityMatrix);
		doc += Markdown.rule() + "\n";


		/* */
		numSlots = 3;
		numProjects = 40;
		numAgents = numProjects * groupSize.maxSize();

		projects = Projects.generated(40, numSlots);
		prefGenerator = new NormallyDistributedProjectPreferencesGenerator(projects, 16);
		generatedDataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		experiment = new Experiment(generatedDataContext, algorithms);

		doc += Markdown.heading("Experiment - " + generatedDataContext.identifier()).toString() + "\n";
		doc += datasetInfo(experiment, numAgents, numProjects, numSlots, groupSize);
		doc += algoResultsInMarkdown(experiment.result().results);
		doc += popularityInMarkdown(experiment.result().popularityMatrix);
		doc += Markdown.rule() + "\n";


		/* */
		numSlots = 3;
		numProjects = 40;
		numAgents = numProjects * groupSize.maxSize();

		projects = Projects.generated(40, numSlots);
		prefGenerator = new UniformProjectPreferencesGenerator(projects);
		generatedDataContext = new GeneratedDataContext(numAgents, projects, groupSize, prefGenerator);

		experiment = new Experiment(generatedDataContext, algorithms);

		doc += Markdown.heading("Experiment - " + generatedDataContext.identifier()).toString() + "\n";
		doc += datasetInfo(experiment, numAgents, numProjects, numSlots, groupSize);
		doc += algoResultsInMarkdown(experiment.result().results);
		doc += popularityInMarkdown(experiment.result().popularityMatrix);
		doc += Markdown.rule() + "\n";


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

		return;
	}

	static String datasetInfo(Experiment experiment, int numAgents, int numProjects, int numSlots, GroupSizeConstraint groupSize)
	{
		var doc = Markdown.heading("Dataset info").toString() + "\n";
		doc += Markdown.image(embed(experiment.projectRankingDistribution.asChart())) + "\n";
		doc += Markdown.unorderedList(
			"\\#agents: " + numAgents,
			"\\#projects: " + numProjects,
			"\\#slots per project: " + numSlots,
			"group sizes, min: " + groupSize.minSize() + ", max: " + groupSize.maxSize()
		) + "\n";

		return doc;
	}

	static String algoResultsInMarkdown(List<Experiment.ExperimentAlgorithmResult> algoResults)
	{
		StringBuilder doc = new StringBuilder();
		for (Experiment.ExperimentAlgorithmResult algoResult : algoResults)
		{
			doc.append(algoResultInMarkdown(algoResult));
		}

		return doc.toString();
	}

	static String algoResultInMarkdown(Experiment.ExperimentAlgorithmResult algoResult)
	{
		String doc = Markdown.heading("Algorithm: " + algoResult.algo.name(), 3).toString() + "\n" +

			Markdown.heading("Student perspective", 4) + "\n" +
			Markdown.image(embed(algoResult.projectProfileCurveStudents.asChart())) + "\n" +
			Markdown.unorderedList(
				"Gini: " + algoResult.giniStudentRanking.asDouble(),
				"AUPCR: " + algoResult.aupcrStudent.asDouble(),
				"Worst rank: " + algoResult.worstAssignedProjectRankOfStudents.asInt()
			) + "\n" +

			Markdown.heading("Group perspective", 4) + "\n" +
//			Markdown.image(embed(algoResult.projectProfileCurveGroup.asChart())) + "\n" +
			Markdown.unorderedList(
				"Gini: " + algoResult.giniGroupAggregateRanking.asDouble(),
				"AUPCR: " + algoResult.aupcrGroup.asDouble()
			) + "\n";

		return doc;
	}

	static String popularityInMarkdown(PopularityMatrix popularityMatrix)
	{
		String doc = Markdown.heading("Algorithm popularity", 4) + "\n" +
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
}
