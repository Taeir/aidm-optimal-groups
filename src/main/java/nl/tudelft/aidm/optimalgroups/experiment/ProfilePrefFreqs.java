package nl.tudelft.aidm.optimalgroups.experiment;

import net.steppschuh.markdowngenerator.table.Table;
import nl.tudelft.aidm.optimalgroups.metric.dataset.ProjectPreferenceProfileFrequencies;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import org.apache.commons.math3.util.Pair;

public class ProfilePrefFreqs
{
	private final ProjectPreferenceProfileFrequencies projectProfFreqs;
	private final int topProjects;
	private final int topProfilesLimit;

	public ProfilePrefFreqs(DatasetContext datasetContext, int topProjectLimit, int topProfilesLimit)
	{
		this.projectProfFreqs = ProjectPreferenceProfileFrequencies.in(datasetContext);
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
