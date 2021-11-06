package nl.tudelft.aidm.optimalgroups.experiment.paper.fairness.report;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import net.steppschuh.markdowngenerator.Markdown;
import net.steppschuh.markdowngenerator.table.Table;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.experiment.viz.FairnessComparisonsTable;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class FairnessVsVanillaQualitySummaryTableReport
{
	private final PregroupingType pregroupingType;
	private final List<FairnessComparisonsTable.Result> resultsAll;
	private final List<FairnessComparisonsTable.Result> resultsSingles;
	private final List<FairnessComparisonsTable.Result> resultsPregrouped;
	private final List<FairnessComparisonsTable.Result> resultsPregroupingUnsatisfied;
	
	private final StringBuffer doc;
	
	public FairnessVsVanillaQualitySummaryTableReport(
			PregroupingType pregroupingType,
			List<FairnessComparisonsTable.Result> resultsAll,
			List<FairnessComparisonsTable.Result> resultsSingles,
			List<FairnessComparisonsTable.Result> resultsPregrouped,
			List<FairnessComparisonsTable.Result> resultsPregroupingUnsatisfied
	) {
		
		this.pregroupingType = pregroupingType;
		
		this.resultsAll = resultsAll;
		this.resultsSingles = resultsSingles;
		this.resultsPregrouped = resultsPregrouped;
		this.resultsPregroupingUnsatisfied = resultsPregroupingUnsatisfied;
		
		this.doc = new StringBuffer();
	}

	public String asMarkdownSource()
	{
		heading("Fairness vs Vanilla comparison", 1);
		
			heading("Info",2);
				text("Pregrouping type: '%s'".formatted(pregroupingType.simpleName()));
		
			heading("All students", 2);
				table(new FairnessComparisonsTable(resultsAll).asMarkdownTable());
				
			heading("'Single' students", 2);
				table(new FairnessComparisonsTable(resultsSingles).asMarkdownTable());
				
			heading("'Pregrouped' students", 2);
				table(new FairnessComparisonsTable(resultsPregrouped).asMarkdownTable());
				
			heading("'Pregrouping unsatisfied' students", 2);
				table(new FairnessComparisonsTable(resultsPregroupingUnsatisfied).asMarkdownTable());
				
		return doc.toString();
	}
	
	public void writeAsHtmlToFile(File file)
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
	
	public String asHtmlSource()
	{
		var markdownSrc = this.asMarkdownSource();
		
		/* Markdown to Html stuff */
		MutableDataSet options = new MutableDataSet();
		options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		Document parsed = parser.parse(markdownSrc);
		var asHtmlSource = renderer.render(parsed);

		return asHtmlSource;
	}
	
	
	private void heading(String value, int level)
	{
		doc.append(Markdown.heading(value, level)).append("\n");
	}
	
	private void unorderedList(String... items)
	{
		doc.append(Markdown.unorderedList((Object[]) items)).append("\n\n\n");
	}
	
	private void text(String text)
	{
		doc.append(Markdown.text(text));
	}
	
	private void text(String format, Object... args)
	{
		text(String.format(format, args));
	}
	
	private void image(JFreeChart chart)
	{
		doc.append(Markdown.image(embed(chart))).append("\n\n");
	}
	
	private void table(Table table)
	{
		doc.append(table).append("\n\n");
	}

	private String embed(JFreeChart chart)
	{
		try {
			var data = ChartUtils.encodeAsPNG(chart.createBufferedImage(1000,800));
			return "data:image/png;base64," + new String(Base64.encodeBase64(data));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void horizontalLine()
	{
		doc.append(Markdown.rule())
			.append("\n");
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
