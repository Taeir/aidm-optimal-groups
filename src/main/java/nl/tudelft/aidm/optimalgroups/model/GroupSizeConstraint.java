package nl.tudelft.aidm.optimalgroups.model;

public interface GroupSizeConstraint
{
	int minSize();
	int maxSize();

	static GroupSizeConstraint basic(int min, int max)
	{
		return new GroupSizeConstraint.Basic(min, max);
	}

	class Basic implements GroupSizeConstraint
	{
		private final int min;
		private final int max;

		public Basic(int min, int max)
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
	}
}
