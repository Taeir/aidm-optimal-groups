package nl.tudelft.aidm.optimalgroups.metric.dataset.projprefbinning;

import nl.tudelft.aidm.optimalgroups.model.pref.ProjectPreference;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BinnableProjPref
{
	public String asLinearOrderInString(int topProjectsLimit)
	{
		Integer[] prefsInArray = preferenceAsArray();
		return Arrays.stream(Arrays.copyOf(prefsInArray, Math.min(prefsInArray.length, topProjectsLimit)))
			.map(String::valueOf)
			.collect(Collectors.joining(" > "));
	}

	protected abstract Integer[] preferenceAsArray();

	public abstract boolean equals(Object o);

	public abstract int hashCode();

	/**
	 * Bins Project preferences <bold>exactly</bold>. That is,
	 * any two project preferences are considered equal if they
	 * contain the same elements in the same order.
	 */
	public static class Exact extends BinnableProjPref
	{
		private final ProjectPreference projectPreference;

		public Exact(ProjectPreference projectPreference)
		{
			this.projectPreference = projectPreference;
		}

		@Override
		protected Integer[] preferenceAsArray()
		{
			return projectPreference.asArray();
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof Exact)) return false;
			Exact that = (Exact) o;
			return projectPreference.equals(that.projectPreference);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(projectPreference);
		}
	}

	/**
	 * Bins Project preferences if their first X ranks match exactly
	 */
	public static class ExactTopXOnly extends BinnableProjPref
	{
		private final ProjectPreference projectPreference;
		private final Integer[] asArrayUpToMaxRank;
		private final int maxRank;

		public ExactTopXOnly(ProjectPreference projectPreference, int maxRank)
		{
			this.projectPreference = projectPreference;
			this.maxRank = maxRank;

			this.asArrayUpToMaxRank = Arrays.copyOf(projectPreference.asArray(), Math.min(maxRank, projectPreference.asArray().length));
		}

		@Override
		protected Integer[] preferenceAsArray()
		{
			return asArrayUpToMaxRank;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof ExactTopXOnly)) return false;
			ExactTopXOnly that = (ExactTopXOnly) o;
			return Arrays.equals(preferenceAsArray(), that.preferenceAsArray());
		}

		@Override
		public int hashCode()
		{
			return Arrays.hashCode(preferenceAsArray());
		}
	}
}
