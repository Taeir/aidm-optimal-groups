package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.BepSysImprovedGroups;
import nl.tudelft.aidm.optimalgroups.algorithm.group.bepsys.BepSysReworked;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.metric.matching.group.PeerPreferenceSatisfaction;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BepSysGroupsTest
{

	@Test
	void reworkWorksSameAsOriginalImproved()
	{
		var courseEdition = CourseEdition.fromLocalBepSysDbSnapshot(10);

		var og = new BepSysImprovedGroups(courseEdition.allAgents(), courseEdition.groupSizeConstraint(), true);
		var reworked = new BepSysReworked(courseEdition.allAgents(), courseEdition.groupSizeConstraint(), true);

		assertEquals(og.asFormedGroups().countDistinctStudents(), reworked.asFormedGroups().countDistinctStudents(), "Groupings do not contain same amnt of students");

		var membersByGroupOg = og.asCollection().stream().map(Group.AbstractGroup::members).collect(Collectors.toList());
		var membersByGroupReworked = reworked.asCollection().stream().map(Group.AbstractGroup::members).collect(Collectors.toList());

//		new GroupPreferenceSatisfaction()

		courseEdition.allAgents().forEach(agent -> {
			var groupInOgAlgo = og.asFormedGroups().asCollection().stream().filter(g -> g.members().asCollection().contains(agent)).findAny().orElseThrow();
			var satisfactionInOg = new PeerPreferenceSatisfaction(groupInOgAlgo, agent);

			var groupInRewAlgo = reworked.asFormedGroups().asCollection().stream().filter(g -> g.members().asCollection().contains(agent)).findAny().orElseThrow();
			var satisfactionInReworked = new PeerPreferenceSatisfaction(groupInOgAlgo, agent);

			if (satisfactionInOg.asFloat() != satisfactionInReworked.asFloat())
				System.out.printf("%s - satisfaction in og: %s, reworked: %s\n", agent, satisfactionInOg.asFraction(), satisfactionInReworked.asFraction());

		});

		assertEquals(membersByGroupOg, membersByGroupReworked);
	}
}