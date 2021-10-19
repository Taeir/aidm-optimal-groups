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
	
	record Manual(int minSize, int maxSize) implements GroupSizeConstraint
	{
		@Override
		public String toString()
		{
			return String.format("GSC[%s,%s]", minSize(), maxSize());
		}
	}
}
