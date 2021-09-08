package nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver.minizinc.model;

import nl.tudelft.aidm.optimalgroups.model.GroupSizeConstraint;
import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.project.Project;

/**
 * A Dataset whose agents and projects' identifiers have been resequenced/remapped to 1-n
 * Use  when algorithms/solvers cannot deal with arbitrary index objects (for example MiniZinc)
 */
public class SequentualDatasetContext implements DatasetContext<SequentualProjects, SequentualAgents>
{
	private final DatasetContext originalContext;
	private final SequentualAgents seqAgents;
	private final SequentualProjects seqProjects;

	public static SequentualDatasetContext from(DatasetContext datasetContext)
	{
		if (datasetContext instanceof SequentualDatasetContext) {
			return (SequentualDatasetContext) datasetContext;
		}

		return new SequentualDatasetContext(datasetContext);
	}

	private SequentualDatasetContext(DatasetContext datasetContext)
	{
		this.seqProjects = SequentualProjects.from(datasetContext.allProjects());
		this.seqAgents = new SequentualAgents(datasetContext.allAgents(), this, seqProjects);
		this.originalContext = datasetContext;
	}

	public DatasetContext originalContext()
	{
		return originalContext;
	}

	public Project mapToOriginal(Project proj)
	{
		return seqProjects.correspondingOriginalProjectOf(proj);
	}

	public Agent mapToOriginal(Agent agent)
	{
		return seqAgents.correspondingOriginalAgentOf(agent);
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
