package nl.tudelft.aidm.optimalgroups.model.dataset.sequentual;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;

/**
 * A Dataset whose agents and projects' identifiers have been resequenced/remapped to 1-n
 * Use  when algorithms/solvers cannot deal with arbitrary index objects (for example MiniZinc)
 */
public class SequentualDataset implements DatasetContext<SequentualProjects, SequentualAgents>
{
	private final DatasetContext originalContext;
	private final SequentualAgents seqAgents;
	private final SequentualProjects seqProjects;

	public static SequentualDataset from(DatasetContext datasetContext)
	{
		if (datasetContext instanceof SequentualDataset) {
			return (SequentualDataset) datasetContext;
		}

		return new SequentualDataset(datasetContext);
	}

	private SequentualDataset(DatasetContext datasetContext)
	{
		this.seqProjects = SequentualProjects.from(datasetContext.allProjects());
		this.seqAgents = new SequentualAgents(datasetContext.allAgents(), this, seqProjects);
		this.originalContext = datasetContext;
	}

	@Override
	public String identifier()
	{
		return originalContext.identifier() + "(reseq)";
	}

	@Override
	public SequentualProjects allProjects()
	{
		return seqProjects;
	}

	@Override
	public SequentualAgents allAgents()
	{
		return seqAgents;
	}

	@Override
	public GroupSizeConstraint groupSizeConstraint()
	{
		return originalContext.groupSizeConstraint();
	}
}
