package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Optional;

public class XVars
{
	private final X[][][] x;
	private final SequentualDatasetContext datasetContext;

	public static XVars createInModel(GRBModel model, SequentualDatasetContext datasetContext)
	{
		var numAgents = datasetContext.allAgents().count();
		var numProjects = datasetContext.allProjects().count();
		var maxSlots = datasetContext.numMaxSlots();

		var x = new X[numAgents+1][numProjects+1][maxSlots];

		// Create the X variables (student assigned to slot)
		datasetContext.allAgents().forEach(student ->
		{
			var s = student.id;

			// hacky: seqentualization does properly handles the varios pref profile types, so workaround
			if (student.projectPreference().isCompletelyIndifferent())
			{
				datasetContext.allProjects().forEach(project ->
				{
					var p = project.id();
					project.slots().forEach(slot ->
					{
						int sl = slot.index();
						x[s][p][sl] = X.createInModel(student, project, slot, model);
					});
				});
			}
			else student.projectPreference().forEach((project, rank) ->
			{
				var p = project.id();
				project.slots().forEach(slot ->
				{
					int sl = slot.index();
					x[s][p][sl] = X.createInModel(student, project, slot, model);
				});
			});
		});

		return new XVars(x, datasetContext);
	}

	private XVars(X[][][] x, SequentualDatasetContext datasetContext)
	{
		this.x = x;
		this.datasetContext = datasetContext;
	}

	public Optional<X> of(Agent agent, Project.ProjectSlot slot)
	{
		return Optional.ofNullable(
			x[agent.id][slot.belongingToProject().id()][slot.index()]
		);
	}
}
