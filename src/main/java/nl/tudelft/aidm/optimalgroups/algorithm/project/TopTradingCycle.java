package nl.tudelft.aidm.optimalgroups.algorithm.project;

import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.match.FormedGroupToProjectSlotMatching;
import nl.tudelft.aidm.optimalgroups.model.match.Match;
import plouchtch.lang.exception.ImplementMe;

import java.util.List;

public class TopTradingCycle implements GroupProjectMatching
{
	public FormedGroupToProjectSlotMatching result()
	{
		throw new ImplementMe();
	}

	@Override
	public List<Match> asList()
	{
		throw new ImplementMe();
	}

	@Override
	public DatasetContext datasetContext()
	{
		throw new ImplementMe();
	}
}
