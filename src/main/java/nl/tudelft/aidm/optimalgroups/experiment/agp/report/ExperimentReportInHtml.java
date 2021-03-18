package nl.tudelft.aidm.optimalgroups.experiment.agp.report;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import nl.tudelft.aidm.optimalgroups.experiment.agp.Experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExperimentReportInHtml
{
	private List<Experiment> experiments;
	
	private String asHtmlSource;
	
	public ExperimentReportInHtml(Experiment... experiments)
	{
		this(List.of(experiments));
	}

	public ExperimentReportInHtml(List<Experiment> experiments)
	{
		this.experiments = experiments;
	}
	
	public ExperimentReportInHtml(String asHtmlSource)
	{
		// Not pretty, this class was intended for generating reports from experiments
		// But the quick n dirty is ok for now...
		this.asHtmlSource = asHtmlSource;
		this.experiments = List.of();
	}

	public String asHtmlSource()
	{
		if (asHtmlSource != null) {
			return asHtmlSource;
		}
		
		/* Quick n dirty: use Markdown report to generate HTML */
		var markdown = new ExperimentReportInMarkdown(experiments).asMarkdownSource();

		/* Markdown to Html stuff */
		MutableDataSet options = new MutableDataSet();
		options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		Document parsed = parser.parse(markdown);
		this.asHtmlSource = renderer.render(parsed);

		return asHtmlSource;
	}

	public void writeHtmlSourceToFile(File file)
	{
		var html = this.asHtmlSource();
		var htmlStyled = htmlWithCss(html);

		try (var writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), false))) {
			writer.write(htmlStyled);
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
