package nl.tudelft.aidm.optimalgroups.algorithm.holistic.exactflow.pessimism;

import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionModNoPeerPref;
import nl.tudelft.aidm.optimalgroups.metric.matching.MatchingMetrics;
import nl.tudelft.aidm.optimalgroups.model.matching.AgentToProjectMatching;
import nl.tudelft.aidm.optimalgroups.model.pref.ListBasedProjectPreferences;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.apache.commons.math3.util.Pair;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Pessimistic
{

	// determine set of 'eccentric' students E - eccentric: student with lowest satisfaction
	// foreach s in E
	//     try all group combinations such that nobody in that group is worse off than s
	//     decrease slots of project p by 1


	public static void main(String[] args)
	{
		int k = 8;

		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(10);
		int minGroupSize = ce.groupSizeConstraint().minSize();

		var result = ce.allAgents().asCollection().stream()
			.map(agent -> agent.projectPreference().asListOfProjects())
			.map(projectPreference -> topNElements(projectPreference, k))
			.flatMap(Collection::stream)
			.collect(Collectors.groupingBy(project -> project)).entrySet().stream()
			.map(entry -> Pair.create(entry.getKey(), entry.getValue().size() / minGroupSize))
			.filter(pair -> pair.getValue() > 0)
			.sorted(Comparator.comparing((Pair<Project, Integer> pair) -> pair.getValue()))
	//			.mapToInt(pair -> pair.getValue())
	//			.sum();
//			.count();
				.collect(Collectors.toList());

//		ce = new CourseEditionModNoPeerPref(ce);
		var bepSysMatchingWhenNoPeerPrefs = new GroupProjectAlgorithm.BepSys().determineMatching(ce);

		var metrics = new MatchingMetrics.StudentProject(AgentToProjectMatching.from(bepSysMatchingWhenNoPeerPrefs));

		return;
	}

	public static <T> List<T> topNElements(List<T> list, int n)
	{
		return list.subList(0, n);
	}
}
