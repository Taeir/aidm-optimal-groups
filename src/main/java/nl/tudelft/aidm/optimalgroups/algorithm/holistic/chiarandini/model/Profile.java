package nl.tudelft.aidm.optimalgroups.algorithm.holistic.chiarandini.model;

import plouchtch.assertion.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface Profile
{
	/**
	 * Iterates in a 'binned' fashion. For each rank the number of agents is given.
	 * For example, a profile (1,1,1,2,2,2,2,5).forEach will give the following results:
	 *  (1, 3), (2,4), (5,1)
	 *
	 *  The iteration order is from rank 1 -> rank n
	 * @param consumer The function accepting iterations
	 */
	void forEach(ProfileConsumer consumer);

	int numInRank(int rank);

	/**
	 * The number of agents of whom the profile is made
	 */
	int size();

	/**
	 *	The higest rank contained in the profile
	 */
	int maxRank();

	/* Supporting types */
		interface ProfileConsumer
		{
			void apply(int rank, int count);
		}


	/* Implementations */
		class listBased implements Profile
		{
			private final ArrayList<Integer> asListOfBins;

			public listBased(List<Integer> profile)
			{
				var numPerRank = profile.stream()
					.collect(Collectors.groupingBy(i -> i, Collectors.counting()));

				asListOfBins = new ArrayList<>();
				asListOfBins.add(0); // 0 index not used

				var maxRank = numPerRank.keySet().stream().mapToInt(value -> value).max()
					.orElse(0);

				for (int i = 1; i <= maxRank; i++)
				{
					asListOfBins.add(numPerRank.getOrDefault(i, 0L).intValue());
				}
			}

			@Override
			public int numInRank(int rank)
			{
				Assert.that(rank <= size()).orThrowMessage("Rank out of bounds");

				return this.asListOfBins.get(rank);
			}

			@Override
			public void forEach(ProfileConsumer consumer)
			{
				for (int rank = 0; rank < asListOfBins.size(); rank++)
				{
					consumer.apply(rank, asListOfBins.get(rank));
				}
			}

			@Override
			public int size()
			{
				return asListOfBins.stream().mapToInt(Integer::intValue).sum();
			}

			@Override
			public int maxRank()
			{
				return asListOfBins.size() - 1;
			}
		}
}
