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
	private final List<Experiment> experiments;

	public ExperimentReportInHtml(Experiment... experiments)
	{
		this(List.of(experiments));
	}

	public ExperimentReportInHtml(List<Experiment> experiments)
	{
		this.experiments = experiments;
	}

	public String asHtmlSource()
	{
		var markdown = new ExperimentReportInMarkdown(experiments).asMarkdownSource();

		/* Markdown to Html stuff */
		MutableDataSet options = new MutableDataSet();
		options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		Document parsed = parser.parse(markdown);
		var html = renderer.render(parsed);

		return html;
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
