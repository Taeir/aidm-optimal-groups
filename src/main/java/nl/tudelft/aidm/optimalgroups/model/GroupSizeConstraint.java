package nl.tudelft.aidm.optimalgroups.model;

public interface GroupSizeConstraint
{
	int minSize();
	int maxSize();

	String toString();

	static GroupSizeConstraint manual(int min, int max)
	{
		return new Manual(min, max);
	}

	class Manual implements GroupSizeConstraint
	{
		private final int min;
		private final int max;

		public Manual(int min, int max)
		{
			this.min = min;
			this.max = max;
		}

		@Override
		public int minSize()
		{
			return min;
		}

		@Override
		public int maxSize()
		{
			return max;
		}

		@Override
		public String toString()
		{
			return String.format("GSC[%s,%s]", minSize(), maxSize());
		}
	}
}
