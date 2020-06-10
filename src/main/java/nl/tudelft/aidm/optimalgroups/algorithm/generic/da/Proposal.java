package nl.tudelft.aidm.optimalgroups.algorithm.generic.da;

public interface Proposal<PROPOSER, PROPOSED>
{
	PROPOSER proposer();
	PROPOSED recipient();

	Integer utilityIfAccepted();
	Integer agentsExpectedUtilityAfterReject();


	interface Actionable<PROPOSER, PROPOSED> extends Proposal<PROPOSER, PROPOSED>
	{
		@FunctionalInterface
		interface AcceptFn<PROPOSER, PROPOSED>
		{
			void tentativelyAccept(Proposal<PROPOSER, PROPOSED> proposal);
		}

		@FunctionalInterface
		interface RejectFn<PROPOSER, PROPOSED>
		{
			void reject(Proposal<PROPOSER, PROPOSED> proposal);
		}

		void tentativelyAccept();
		void reject();

		Proposal<PROPOSER, PROPOSED> proposal();
	}
}
