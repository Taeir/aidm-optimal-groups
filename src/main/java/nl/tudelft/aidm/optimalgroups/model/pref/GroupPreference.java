package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface GroupPreference
{
	// TODO: determine representation (let algo guide this choice)
	// for now as int array purely for memory efficiency
	int[] asArray();

	List<Agent> asList();

}
