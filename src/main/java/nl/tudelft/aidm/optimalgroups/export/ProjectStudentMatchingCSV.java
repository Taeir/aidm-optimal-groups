package nl.tudelft.aidm.optimalgroups.export;

import nl.tudelft.aidm.optimalgroups.model.matching.FormedGroupToProjectMatching;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProjectStudentMatchingCSV
{
	private final FormedGroupToProjectMatching matching;
	
	public ProjectStudentMatchingCSV(FormedGroupToProjectMatching matching)
	{
		this.matching = matching;
	}
	
	public void writeToFile(String fileRelativeToReports)
	{
		var file = new File("reports/" + fileRelativeToReports + ".csv");
		
		
		try (var writer = new FileWriter(file))
		{
			for (var match : matching.asList())
			{
				var projId = "" + match.to().id();
				writer.write(projId);
				
				for (var member : match.from().members())
				{
					writer.write(',');
					writer.write(member.id.toString());
				}
				writer.write("\n");
			}
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
