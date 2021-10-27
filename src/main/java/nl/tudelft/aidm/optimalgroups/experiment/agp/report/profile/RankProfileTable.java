package nl.tudelft.aidm.optimalgroups.experiment.agp.report.profile;


import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.table.TableRow;
import nl.tudelft.aidm.optimalgroups.model.HasProjectPrefs;
import nl.tudelft.aidm.optimalgroups.model.Profile;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.Matching;
import nl.tudelft.aidm.optimalgroups.model.pref.rank.RankInPref;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record RankProfileTable(SeriesData... dataseries)
{
	public Table asMarkdownTable()
	{
		var minRank = 1;
		var maxRank = Arrays.stream(dataseries)
				.map(SeriesData::profile)
				.mapToInt(Profile::maxRank)
				.max();
		
		Assert.that(maxRank.isPresent()).orThrowMessage("Dataseries are empty, cannot make a table");
		
		var tableBuilder = new Table.Builder();
		
		var headerAsList = new ArrayList<>();
		headerAsList.add("Student type");
		var range = IntStream.rangeClosed(minRank, maxRank.getAsInt()).boxed().toList();
		headerAsList.addAll(range);
		var header = new TableRow(headerAsList);
		
		tableBuilder.addRow(header);
		
		for (SeriesData dataserie : dataseries)
		{
			var asList = new ArrayList<>();
			var alignments = new ArrayList<Integer>();
			
			asList.add(dataserie.studentType);
			alignments.add(Table.ALIGN_LEFT);
			
			for (int rank = minRank; rank <= maxRank.getAsInt(); rank++)
			{
				asList.add(dataserie.profile().numAgentsWithRank(rank));
				alignments.add(Table.ALIGN_RIGHT);
			}
			
			var asRow = new TableRow(asList);
			tableBuilder.addRow(asRow);
			tableBuilder.withAlignments(alignments);
		}
		
		return tableBuilder.build();
	}
	
	public Integer[][] asArray()
	{
		var dataset = dataseries();
		var maxRank = Arrays.stream(dataset).map(SeriesData::profile).mapToInt(Profile::maxRank).max().orElse(0);
		
		var asArray = new Integer[dataset.length][maxRank];
		
		for (var i = new AtomicInteger(0); i.get() < dataseries().length; i.getAndIncrement())
		{
			var rowData = dataset[i.get()].profile();
			rowData.forEach((rank, count) -> asArray[i.get()][rank] = count);
		}
		
		return asArray;
	}
	
	public static record SeriesData(Profile profile, String studentType)
	{
		public static SeriesData from(Matching<Agent, Project> matching, String studentType)
		{
			var profile = Profile.of(matching);
			
			return new SeriesData(profile, studentType);
		}
	}
}
