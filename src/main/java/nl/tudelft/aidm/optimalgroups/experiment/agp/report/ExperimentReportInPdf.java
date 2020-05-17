package nl.tudelft.aidm.optimalgroups.experiment.agp.report;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import nl.tudelft.aidm.optimalgroups.experiment.agp.Experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ExperimentReportInPdf
{
	private final List<Experiment> experiments;

	public ExperimentReportInPdf(Experiment... experiments)
	{
		this(List.of(experiments));
	}

	public ExperimentReportInPdf(List<Experiment> experiments)
	{
		this.experiments = experiments;
	}

	public String asHtmlSource()
	{
		var markdown = new ExperimentReportInMarkdown(experiments).asMarkdownSource();

		/* Markdown to Html stuff */
		MutableDataSet options = new MutableDataSet();

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		Document parsed = parser.parse(markdown);
		var html = renderer.render(parsed);

		return html;
	}

//	public void writePdfToFile(File file)
//	{
//		var html = this.asHtmlSource();
//
//		try (var writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), false))) {
//			writer.write(html);
//		}
//		catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}


	public void writePdfToFile(File file)
	{
		var html = new ExperimentReportInHtml(experiments).asHtmlSource();

		MutableDataSet options = new MutableDataSet();

		try {
			PdfConverterExtension.exportToPdf(file.getAbsolutePath(), html, "", options);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


}
