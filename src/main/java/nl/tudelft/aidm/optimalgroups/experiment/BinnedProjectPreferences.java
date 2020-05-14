package nl.tudelft.aidm.optimalgroups.experiment;

import net.steppschuh.markdowngenerator.table.Table;
import nl.tudelft.aidm.optimalgroups.metric.dataset.projprefbinning.BinnableProjPref;
import nl.tudelft.aidm.optimalgroups.metric.dataset.projprefbinning.ProjectPreferenceProfileFrequencies;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.function.Function;

public class BinnedProjectPreferences
{
	private final ProjectPreferenceProfileFrequencies projectProfFreqs;
	private final int topProjects;
	private final int topProfilesLimit;

	public static BinnedProjectPreferences exactBins(DatasetContext datasetContext, int maxRankForPrint, int topProfilesLimit)
	{
		return new BinnedProjectPreferences(datasetContext, maxRankForPrint, topProfilesLimit, BinnableProjPref.Exact::new);
	}

	public static BinnedProjectPreferences exactTopRanksBins(DatasetContext datasetContext, int maxRank, int topProfilesLimit)
	{
		Function<ProjectPreference, BinnableProjPref> binType = (pp) -> new BinnableProjPref.ExactTopXOnly(pp, maxRank);
		return new BinnedProjectPreferences(datasetContext, maxRank, topProfilesLimit, binType);
	}

	public BinnedProjectPreferences(DatasetContext datasetContext, int topProjectLimit, int topProfilesLimit, Function<ProjectPreference,BinnableProjPref> bin)
	{
		this.projectProfFreqs = new ProjectPreferenceProfileFrequencies(datasetContext.allAgents(), bin);
		this.topProjects = topProjectLimit;
		this.topProfilesLimit = topProfilesLimit;
	}

	public String asMarkdownTable()
	{
		var mostPopular = projectProfFreqs.mostPopular(topProfilesLimit);

		var table = new Table.Builder();
		table.addRow("Profile", "# occurences");
		table.withAlignments(Table.ALIGN_CENTER, Table.ALIGN_RIGHT);
		for (var topProfile : mostPopular)
		{
			String profileAsString = topProfile.getKey().asLinearOrderInString(topProjects);
			Long occurences = topProfile.getValue();

			table.addRow(profileAsString, occurences);
		}

		return table.build().serialize();
	}

	String asMarkdownChart()
	{
		return "";
	}
}
