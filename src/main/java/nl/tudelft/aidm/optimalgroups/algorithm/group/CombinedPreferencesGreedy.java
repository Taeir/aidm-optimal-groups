package nl.tudelft.aidm.optimalgroups.algorithm.group;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.FormedGroups;
import nl.tudelft.aidm.optimalgroups.model.Group;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreferenceOfAgents;
import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Map.Entry.*;

public class CombinedPreferencesGreedy implements GroupFormingAlgorithm {

    private Agents students;
    private final GroupSizeConstraint groupSizeConstraint;
    private Map<String, Agent> availableStudents;
    private Map<String, Agent> unavailableStudents;

    private FormedGroups formedGroups;

    private boolean done = false;

    public CombinedPreferencesGreedy(Agents students, GroupSizeConstraint groupSizeConstraint) {
        this.students = students;
        this.groupSizeConstraint = groupSizeConstraint;

        this.initializeObjects(students);
    }

    private void initializeObjects(Agents students) {
        this.formedGroups = new FormedGroups();
        this.availableStudents = new HashMap<>(students.count());
        this.unavailableStudents = new HashMap<>(students.count());

        this.students.forEach(student -> {
            this.availableStudents.put(student.name, student);
        });
    }

    private void constructGroups() {
        if (this.formedGroups.count() > 0) {
            initializeObjects(this.students);
        }

        this.students.useCombinedPreferences();

        // Construct a list of agent sorted (descending) by the size of their group preference (as an attempt for a nice heuristic)
        List<Agent> sortedList = new ArrayList<>(this.students.asCollection());
        sortedList.sort(Comparator.comparing(Agent::groupPreferenceLength).reversed());

        SetOfGroupSizesThatCanStillBeCreated groupSizes = new SetOfGroupSizesThatCanStillBeCreated(this.students.count(), groupSizeConstraint, SetOfGroupSizesThatCanStillBeCreated.FocusMode.MAX_FOCUS);

        // Start iterating and forming groups greedily
        for (Agent student : sortedList) {
            if (this.unavailableStudents.containsKey(student.name)) {
                continue;
            }

            Map<String, Integer> differences = new HashMap<>(this.availableStudents.size());

            for (Agent other : this.students.asCollection()) {

                if (student.equals(other) || this.unavailableStudents.containsKey(other.name)) {
                    continue;
                }

                int differenceToOther = student.projectPreference.differenceTo(other.projectPreference);
                differences.put(other.name, differenceToOther);
            }

            // Sort differences in ascending order (least difference first)
            List<Map.Entry<String, Integer>> sortedDifferences = new ArrayList<>(differences.entrySet());
            sortedDifferences.sort(comparingByValue());


            List<Agent> agents = new ArrayList<>(groupSizeConstraint.maxSize());

            // Put the student in the group
            this.availableStudents.remove(student.name);
            this.unavailableStudents.put(student.name, student);
            agents.add(student);

            // Determine the group size
            int groupSize = groupSizeConstraint.maxSize();
            while (groupSizes.mayFormGroupOfSize(groupSize) == false && groupSize >= groupSizeConstraint.minSize()) {
                groupSize--;
            }

            // Start inserting the remaining group members (students of which the combined preference has the least difference)
            for (Map.Entry<String, Integer> entry : sortedDifferences) {
                if (agents.size() >= groupSize) {
                    break;
                }

                Agent removed = this.availableStudents.remove(entry.getKey());
                this.unavailableStudents.put(entry.getKey(), removed);

                agents.add(removed);
            }

            // Transform the new agents into a formed group
            Agents newGroupAgents = Agents.from(agents);
            ProjectPreference aggregatedPreference = ProjectPreferenceOfAgents.aggregateWithGloballyConfiguredAggregationMethod(newGroupAgents);
            Group.TentativeGroup newTentativeGroup = new Group.TentativeGroup(newGroupAgents, aggregatedPreference);
            this.formedGroups.addAsFormed(newTentativeGroup);

            // Inform the group sizes object that a group of this size has been formed
            groupSizes.recordGroupFormedOfSize(groupSize);
        }

        this.students.useDatabasePreferences();
    }

    @Override
    public FormedGroups asFormedGroups() {
        if (this.done == false) {
            this.constructGroups();
        }

        return this.formedGroups;
    }

    @Override
    public Collection<Group.FormedGroup> asCollection() {
        return this.asFormedGroups().asCollection();
    }

    @Override
    public void forEach(Consumer<Group.FormedGroup> fn) { this.formedGroups.forEach(fn); }

    @Override
    public int count() { return this.formedGroups.count(); }
}
