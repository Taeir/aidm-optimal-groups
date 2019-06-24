package nl.tudelft.aidm.optimalgroups.model.match;

/**
 * The match between a group/agent and project
 */
public interface Match<FROM, TO>
{
	FROM from();
	TO to();
}
