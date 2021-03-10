package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import gurobi.GRBModel;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

import java.util.Optional;

public class YVars
{
	private final Y[][] y;
	private final SequentualDatasetContext datasetContext;

	public static YVars createInModel(GRBModel model, SequentualDatasetContext datasetContext)
	{
		var numProjects = datasetContext.allProjects().count();
		var maxSlots = datasetContext.numMaxSlots();

		var y = new Y[numProjects+1][maxSlots];

		// Create Y variables (project-slot is open)
		datasetContext.allProjects().forEach(project ->
		{
			int p = project.id();
			project.slots().forEach(slot -> {
				var sl = slot.index();
				y[p][sl] = Y.createInModel(slot, model);
			});
		});

		return new YVars(y, datasetContext);
	}

	public YVars(Y[][] y, SequentualDatasetContext datasetContext)
	{
		this.y = y;
		this.datasetContext = datasetContext;
	}
	public Optional<Y> of(Project.ProjectSlot slot)
	{
		// Todo: check if within bounds

		return Optional.ofNullable(
			y[slot.belongingToProject().id()][slot.index()]
		);
	}
}
