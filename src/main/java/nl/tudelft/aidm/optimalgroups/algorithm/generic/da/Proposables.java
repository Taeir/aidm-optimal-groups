package nl.tudelft.aidm.optimalgroups.algorithm.generic.da;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Proposables<PROPOSER, PROPOSABLE> implements Iterable<Proposable<PROPOSER, PROPOSABLE>>
{
	private final Collection<Proposable<PROPOSER, PROPOSABLE>> proposables;

	public Proposables(Collection<PROPOSABLE> proposables, Function<PROPOSABLE, Integer> capacityFunction)
	{
		this.proposables = proposables.stream()
			.map(proposable -> (Proposable<PROPOSER, PROPOSABLE>)
				new Proposable.Generic<PROPOSER, PROPOSABLE>(proposable, capacityFunction.apply(proposable)))
			.collect(Collectors.toList());
	}

	public Proposables(Collection<Proposable<PROPOSER, PROPOSABLE>> asCollection)
	{
		this.proposables = asCollection;
	}

	void receiveProposal(Proposal.Actionable<PROPOSER, PROPOSABLE> proposal)
	{
		// UGLY and inefficient: FIXME!
		var projToProposeTo = proposables.stream()
			.filter(proposableProject -> proposableProject.underlying().equals(proposal.proposal().recipient()))
			.findAny()
			.get();

		projToProposeTo.handleProposal(proposal);
	}

	@NotNull
	@Override
	public Iterator<Proposable<PROPOSER, PROPOSABLE>> iterator()
	{
		return proposables.iterator();
	}
}
