package nl.tudelft.aidm.optimalgroups.integration;

import nl.tudelft.aidm.optimalgroups.Application;
import nl.tudelft.aidm.optimalgroups.algorithm.GroupProjectAlgorithm;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.MILP_Mechanism_FairPregrouping;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.Constraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.FixMatchingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.constraints.grouping.HardGroupingConstraint;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model.PregroupingType;
import nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.objectives.OWAObjective;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEditionFromDb;
import nl.tudelft.aidm.optimalgroups.experiment.paper.fairness.report.FairnessVsVanillaQualityExperimentReport;
import nl.tudelft.aidm.optimalgroups.export.ProjectStudentMatchingJSON;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.agent.Agents;
import nl.tudelft.aidm.optimalgroups.model.group.Group;
import nl.tudelft.aidm.optimalgroups.model.group.Groups;
import nl.tudelft.aidm.optimalgroups.model.pref.AggregatedProjectPreference;
import nl.tudelft.aidm.optimalgroups.model.project.Project;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PFIntegration {

    /**
     * Creates a grouping and outputs the results.
     *
     * @see #createGrouping(String[])
     */
    public static void main(String[] args) {
        ensureSqliteLoaded();
        JSONObject json = createGrouping(args);
        System.out.println(json);
    }

    /**
     * Creates a grouping from a JSON configuration.
     * Handles JSON of the following form:
     * <pre>
     *     {
     *         course_edition_id: 1,
     *         pregrouping: {
     *             type: conditional                        -> [soft/hard/conditional]
     *             size_bound: true                         -> true/false, whether to use the maximum size
     *             up_to_including_rank: 5                  -> if type is conditional, what is considered a valid rank?
     *         },
     *         fixed: {
     *             preference_aggregation_method: Borda     -> [Copeland/Borda] way in which preferences of a group are aggregated
     *             group_matching: [[<uid>, ...], ...]      -> array of arrays, the groups that need to be met
     *             project_matching: [                      -> array of user(s) to project matches that must be met
     *                 {
     *                     users: [<uid>, ...],
     *                     project: <pid>
     *                 },
     *                 {
     *                     user: <uid>,
     *                     project: <pid>
     *                 },
     *                 ...
     *             ]
     *         }
     *     }
     * </pre>
     * @param args the arguments, expects a single argument representing the json listed above
     * @return a json object with the details of the grouping
     */
    public static JSONObject createGrouping(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Please give a single argument, the JSON settings for the matching");
        }

        // Load the json
        JSONObject json = new JSONObject(args[0]);

        // Set the preference aggregation method in case it is explicitly set
        Application.preferenceAggregatingMethod =
                Optional.ofNullable((String) json.optQuery("/fixed/preference_aggregation_method"))
                        .orElse(Application.preferenceAggregatingMethod);

        // Load from the database (config.properties)
        int courseEditionId = json.getInt("course_edition_id");
        var datasetContext = CourseEditionFromDb.fromSettingsDb(courseEditionId);

        // Set the pregrouping type
        var pregroupingType = parsePregrouping(json, datasetContext);
        var pregrouping = pregroupingType.instantiateFor(datasetContext);

        // Fix users to projects
        var matchFixes = parseUserProjectFixes(json, datasetContext);

        // Fix users in groups
        var groupFixes = parseUserGroupFixes(json, datasetContext);

        // Combine the constraints together
        var constraints = new ArrayList<Constraint>(matchFixes.size() + groupFixes.size());
        constraints.addAll(matchFixes);
        constraints.addAll(groupFixes);
        var objective = new OWAObjective();

        // Perform the matching
        var matchingFair = new MILP_Mechanism_FairPregrouping(
                datasetContext,
                objective,
                pregroupingType,
                constraints.toArray(new Constraint[0])
        ).doIt();

        // Report results
        var jsonOutput = new ProjectStudentMatchingJSON(matchingFair.finalMatching());
        var report = new FairnessVsVanillaQualityExperimentReport(datasetContext, pregrouping, List.of(
                new GroupProjectAlgorithm.Result(new GroupProjectAlgorithm.Chiarandini_Fairgroups(objective, pregroupingType), matchingFair)
        ));

        // Put it in JSON
        JSONObject jsonOut = new JSONObject();
        jsonOut.put("matching", jsonOutput.toJSON());
        jsonOut.put("statistics_html", report.asHtmlSource());
        return jsonOut;
    }

    /**
     * Parses the pregrouping part of the config.
     *
     * @param json the json object
     * @param datasetContext the dataset
     * @return the PregroupingType
     */
    public static PregroupingType parsePregrouping(JSONObject json, CourseEditionFromDb datasetContext) {
        JSONObject pregrouping = json.getJSONObject("pregrouping");
        String type = pregrouping.getString("type");
        boolean sizeBound = pregrouping.getBoolean("size_bound");
        int maxsize = datasetContext.groupSizeConstraint().maxSize();

        switch (type) {
            case "soft":
                if (sizeBound) {
                    return PregroupingType.sizedCliqueSoftGrouped(maxsize);
                } else {
                    return PregroupingType.anyCliqueSoftGrouped();
                }
            case "hard":
                if (sizeBound) {
                    return PregroupingType.sizedCliqueHardGrouped(maxsize);
                } else {
                    return PregroupingType.anyCliqueHardGrouped();
                }
            case "conditional":
                int upToRank = pregrouping.getInt("up_to_including_rank");
                if (sizeBound) {
                    return PregroupingType.sizedCliqueConditionallyGrouped(upToRank, maxsize);
                } else {
                    return PregroupingType.anyCliqueConditionallyGrouped(upToRank);
                }
            default:
                throw new RuntimeException("Unknown pregrouping type: " + type);
        }
    }

    /**
     * Parses the given json for matching constraints (specific user(s) to a specific project).
     *
     * @param json the json
     * @param datasetContext the dataset
     * @return the matching constraints
     */
    public static List<Constraint> parseUserProjectFixes(JSONObject json, CourseEditionFromDb datasetContext) {
        JSONObject fixed = json.optJSONObject("fixed");
        if (fixed == null) return List.of();

        JSONArray matching = fixed.optJSONArray("project_matching");
        if (matching == null) return List.of();

        List<Constraint> fixes = new ArrayList<>();
        for (Object obj : matching) {
            JSONObject j = (JSONObject) obj;

            // Determine the project
            int projectId = j.getInt("project");
            Project project = datasetContext.findProjectByProjectId(projectId)
                    .orElseThrow(() -> new RuntimeException("Unable to find project with id " + projectId));

            // Determine the users and add to the list of FixMatchingConstraint
            if (j.has("users")) {
                j.getJSONArray("users").toList().stream()
                        .map(o -> datasetContext.findAgentByUserId((int) o)
                                .orElseThrow(() -> new RuntimeException("Unable to find user with id " + o)))
                        .map(a -> new FixMatchingConstraint(a, project))
                        .forEach(fixes::add);
            } else if (j.has("user")) {
                int userId = j.getInt("user");
                var agent = datasetContext.findAgentByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("Unable to find user with id " + userId));
                fixes.add(new FixMatchingConstraint(agent, project));
            }
        }

        return fixes;
    }

    /**
     * Parses the given json for grouping constraints (specific users together in a group).
     *
     * @param json the json
     * @param datasetContext the dataset
     * @return the grouping constraints
     */
    public static List<Constraint> parseUserGroupFixes(JSONObject json, CourseEditionFromDb datasetContext) {
        JSONObject fixed = json.optJSONObject("fixed");
        if (fixed == null) return List.of();

        JSONArray matching = fixed.optJSONArray("group_matching");
        if (matching == null) return List.of();

        List<Constraint> fixes = new ArrayList<>();
        for (Object obj : matching) {
            // Determine the users involved
            List<Agent> agentsList = ((JSONArray) obj).toList().stream()
                    .map(o -> datasetContext.findAgentByUserId((int) o)
                            .orElseThrow(() -> new RuntimeException("Unable to find user with id " + o)))
                    .collect(Collectors.toList());
            var agents = Agents.from(agentsList);

            // Aggregate preferences
            var aggregatedPreferences = AggregatedProjectPreference.usingGloballyConfiguredMethod(agents);

            // Create a tentative group
            Group group = new Group.TentativeGroup(agents, aggregatedPreferences);

            // Fix them together
            fixes.add(new HardGroupingConstraint(Groups.of(group)));
        }

        return fixes;
    }

    /**
     * Ensures that SQLite is loaded if it is available.
     */
    private static void ensureSqliteLoaded() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            // Silently ignore in case sqlite is not found
        }
    }
}
