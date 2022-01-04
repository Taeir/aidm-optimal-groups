package nl.tudelft.aidm.optimalgroups.export;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Exports matching of Bepsys agents to a json export
 */
public class ProjectStudentMatchingJSON
{
	private final GroupToProjectMatching<Group.FormedGroup> matching;

	public ProjectStudentMatchingJSON(GroupToProjectMatching<Group.FormedGroup> matching)
	{
		this.matching = matching;
	}

	/**
	 * Converts this matching to JSON format:
	 * <pre>
	 *     {
	 *         <projectid>: [<userid>, ...],
	 *         ...
	 *     }
	 * </pre>
	 *
	 * @return the json
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		for (var match : matching.asList()) {
			var project = (Project.BepSysProject) match.to();
			var users = match.from().members().asCollection().stream()
					.map(a -> ((Agent.AgentInBepSysSchemaDb) a).bepSysUserId)
					.collect(Collectors.toList());
			json.put(String.valueOf(project.bepsysId), users);
		}
		return json;
	}

	public void writeToFile(String fileRelativeToReports) {
		JSONObject json = toJSON();
		var file = new File("reports/" + fileRelativeToReports + ".json");

		try (var writer = new BufferedWriter(new FileWriter(file))) {
			json.write(writer);
		} catch (IOException ex) {
			throw new RuntimeException("Unable to write report to file", ex);
		}
	}
}
