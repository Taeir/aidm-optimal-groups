package nl.tudelft.aidm.optimalgroups.dataset.generated.prefs;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import plouchtch.assertion.Assert;

import java.util.Arrays;

public interface PregroupingGenerator
{
	/**
	 * Draws a pregrouping size
	 * @return A pregrouping size
	 */
	Integer draw();
	
	record ChancePerTypeBased(Item... items) implements PregroupingGenerator
	{
		@Override
		public Integer draw()
		{
			var values = Arrays.stream(items).mapToInt(Item::groupSize).toArray();
			var probabilies = Arrays.stream(items).mapToDouble(Item::chance).toArray();
			var distribution = new EnumeratedIntegerDistribution(values, probabilies);
			
			return distribution.sample();
		}
		
		public record Item(Integer groupSize, Double chance) {}
		
		public ChancePerTypeBased {
			var chanceSum = Arrays.stream(items).mapToDouble(Item::chance).sum();
			Assert.that(chanceSum == 1.0).orThrowMessage("Chances must sum up to 1.0");
		}
	}
	
	static PregroupingGenerator CE10Like()
	{
		// sortof based on CE10
		return new PregroupingGenerator.ChancePerTypeBased(
				new ChancePerTypeBased.Item(1, 0.3),
				new ChancePerTypeBased.Item(2, 0.03),
				new ChancePerTypeBased.Item(3, 0.15),
				new ChancePerTypeBased.Item(4, 0.27),
				new ChancePerTypeBased.Item(5, 0.25)
		);
	}
	
	static PregroupingGenerator none()
	{
		return () -> 1;
	}
}
