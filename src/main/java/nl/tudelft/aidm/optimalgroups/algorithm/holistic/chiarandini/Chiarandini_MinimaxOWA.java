package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini;

import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.Pregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.dataset.generated.GeneratedDataContext;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.metric.profile.StudentRankProfile;
import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.matching.GroupToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import nl.tudelft.aidm.optimalgroups.model.project.Projects;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Chiarandini_MinimaxOWA
{
	private final DatasetContext datasetContext;
	private final Pregrouping pregrouping;
	
	public Chiarandini_MinimaxOWA(DatasetContext datasetContext, PregroupingType pregroupingType)
	{
		this.datasetContext = datasetContext;
		this.pregrouping = pregroupingType.instantiateFor(datasetContext);
	}

	public GroupToProjectMatching<Group.FormedGroup> doIt()
	{
		var objFn = new OWAObjective();
		
		return new ChiarandiniBaseModel(datasetContext, objFn, pregrouping.constraint()).doIt();
	}

	/* test */
	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEditionFromDb.fromLocalBepSysDbSnapshot(1);
		//DatasetContext<Projects, Agents> ce = GeneratedDataContext.withNormallyDistributedProjectPreferences(150, 40, GroupSizeConstraint.manual(4,5), 4);

		var owaMinimaxChiarandini = new Chiarandini_MinimaxOWA(ce, PregroupingType.sizedCliqueHardGrouped(5));
		var resultOwa = owaMinimaxChiarandini.doIt();
		
		var agentToProjectMatching = AgentToProjectMatching.from(resultOwa);

		for (Map.Entry<Project, List<Agent>> e : agentToProjectMatching.groupedByProject().entrySet()) {
			System.out.println(e.getKey().name() + ": " + e.getValue().stream().map(a -> ((Agent.AgentInBepSysSchemaDb) a).bepSysUserId.toString()).collect(Collectors.joining(",")));
		}

		var metricsOwa = new MatchingMetrics.StudentProject(agentToProjectMatching);
		new StudentRankProfile(agentToProjectMatching).printResult(System.out);
		new StudentRankProfile(agentToProjectMatching).displayChart("Chiarandini minimax-owa");

		return;
	}
	
	
}
