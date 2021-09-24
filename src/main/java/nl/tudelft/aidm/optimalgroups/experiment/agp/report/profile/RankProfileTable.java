package nl.tudelft.aidm.optimalgroups.experiment.agp.report.profile;


import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.table.TableRow;
import nl.tudelft.aidm.optimalgroups.model.HasProjectPrefs;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record RankProfileTable(SeriesData... dataseries)
{
	
	public Table asMarkdownTable()
	{
		var minRank = Arrays.stream(dataseries)
				.flatMapToInt(seriesData -> seriesData.profile.keySet().stream().mapToInt(Integer::intValue))
				.min();
		
		var maxRank = Arrays.stream(dataseries)
				.flatMapToInt(seriesData -> seriesData.profile.keySet().stream().mapToInt(Integer::intValue))
				.max();
		
		Assert.that(minRank.isPresent()).orThrowMessage("Dataseries are empty, cannot make a table");
		
		var tableBuilder = new Table.Builder();
		tableBuilder.addRow(new TableHeader(minRank.getAsInt(), maxRank.getAsInt()).asTableRow());
		
		for (SeriesData dataserie : dataseries)
		{
			var asList = new ArrayList<>();
			var alignments = new ArrayList<Integer>();
			
			asList.add(dataserie.studentType);
			alignments.add(Table.ALIGN_LEFT);
			
			for (int rank = minRank.getAsInt(); rank <= maxRank.getAsInt(); rank++)
			{
				asList.add(dataserie.profile().getOrDefault(rank, 0L));
				alignments.add(Table.ALIGN_RIGHT);
			}
			
			var asRow = new TableRow(asList);
			tableBuilder.addRow(asRow);
			tableBuilder.withAlignments(alignments);
		}
		
		return tableBuilder.build();
	}
	
	private static record TableHeader(Integer minRank, Integer maxRank)
	{
		TableRow asTableRow()
		{
			var asList = new ArrayList<>();
			asList.add("Student type");
			
			var range = IntStream.rangeClosed(minRank, maxRank).boxed().toList();
			asList.addAll(range);
			
			return new TableRow(asList);
		}
	}
	
	public static record SeriesData(String studentType, Map<Integer, Long> profile)
	{
		public static SeriesData from(Matching<? extends HasProjectPrefs, Project> matching, String studentType)
		{
			var profile = matching.asList().stream().map(match -> match.from().projectPreference().rankOf(match.to()))
					.filter(RankInPref::isPresent)
					.collect(Collectors.groupingBy(RankInPref::asInt, Collectors.counting()));
			
			return new SeriesData(studentType, profile);
		}
	}
}
