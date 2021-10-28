package nl.tudelft.aidm.optimalgroups.model.comparison;

import nl.tudelft.aidm.optimalgroups.model.Profile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ParetoComperatorTest
{
	@Test
	void test_paretoComparison_profilesSame()
	{
		var profile = Profile.fromProfileArray(1,4,3,2);
		
		assertEquals(ParetoComperator.ParetoOutcome.SAME, new ParetoComperator().compare(profile, profile));
		assertEquals(ParetoComperator.ParetoOutcome.SAME, new ParetoComperator().compare(Profile.fromProfileArray(1,4,3,2), profile));
	}
	
	@Test
	void test_paretoComparison_profilesNotPareto()
	{
		var profile = Profile.fromProfileArray(1,4,3,2);
		var other = Profile.fromProfileArray(1,3,5,1);
		
		assertEquals(ParetoComperator.ParetoOutcome.NONE, new ParetoComperator().compare(profile, other));
		assertEquals(ParetoComperator.ParetoOutcome.NONE, new ParetoComperator().compare(other, profile));
	}
	
	@Test
	void test_paretoComparison_profilesPareto()
	{
		var profile = Profile.fromProfileArray(1,5,8,10);
		var other = Profile.fromProfileArray(1,4,9,10);
		
		assertEquals(ParetoComperator.ParetoOutcome.BETTER, new ParetoComperator().compare(profile, other));
		assertEquals(ParetoComperator.ParetoOutcome.WORSE, new ParetoComperator().compare(other, profile));
	}
	
	@Test
	void test_paretoComparison_profilesNotPareto2()
	{
		var profile = Profile.fromProfileArray(1,5,7,11);
		var other = Profile.fromProfileArray(1,4,9,10);
		
		var betterOrSame = new ParetoComperator().isParetoBetter(profile, other);
		
		assertFalse(betterOrSame);
	}
}