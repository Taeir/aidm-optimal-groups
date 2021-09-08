package nl.tudelft.aidm.optimalgroups.export;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.matching.FormedGroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import plouchtch.assertion.Assert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exports matching of Bepsys agents to a csv of id's
 */
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
				var project = (Project.BepSysProject) match.to();
				var projId = "" + project.bepsysId;
				writer.write(projId);
				
				for (var member : match.from().members())
				{
					Assert.that(member instanceof Agent.AgentInBepSysSchemaDb).orThrowMessage("Agent is not a bepsys agent, can't extract id for export");
					var bepsysUserId = ((Agent.AgentInBepSysSchemaDb) member).bepSysUserId;
					
					writer.write(',');
					writer.write(bepsysUserId);
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
