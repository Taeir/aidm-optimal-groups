package nl.tudelft.aidm.optimalgroups.experiment.paper.fairness;

import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.experiment.agp.report.ExperimentReportInHtml;
import nl.tudelft.aidm.optimalgroups.experiment.dataset.ResearchProject2021Q4Dataset;
import nl.tudelft.aidm.optimalgroups.experiment.paper.fairness.report.FairnessVsVanillaQualityExperimentReport;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FairnessVsVanillaQualityExperiments
{
	public static void main(String[] args)
	{
		var experimentsRunId = Instant.now().getEpochSecond();
		
		var pregroupingType = PregroupingType.anyCliqueSoftGrouped();
		
		var algorithms = List.of(
			new GroupProjectAlgorithm.Chiarandini_MiniMax_OWA(pregroupingType),
			new GroupProjectAlgorithm.Chiarandini_Fairgroups(new OWAObjective(), pregroupingType)
		);
		
		var datasets = List.of(
				CourseEditionFromDb.fromLocalBepSysDbSnapshot(4),
				CourseEditionFromDb.fromLocalBepSysDbSnapshot(10),
				ResearchProject2021Q4Dataset.getInstance()
		);
		
		for (CourseEdition datasetContext : datasets)
		{
			var pregrouping = pregroupingType.instantiateFor(datasetContext);
			
			var results = new ArrayList<GroupProjectAlgorithm.Result>();
			for (GroupProjectAlgorithm algorithm : algorithms)
			{
				var matching = algorithm.determineMatching(datasetContext);
				var result = new GroupProjectAlgorithm.Result(algorithm, matching);
				results.add(result);
			}
			
			var fileName = String.format("fairness_%s_CE(%s)-%s", experimentsRunId, datasetContext.bepSysId(), pregroupingType.simpleName());
			
			new FairnessVsVanillaQualityExperimentReport(datasetContext, pregrouping, results)
					.writeAsHtmlToFile(new File("reports/thesis/" + fileName + ".html"));
			// result into document
			
		}
	}
}
