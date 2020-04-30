package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface GroupPreference
{
	// TODO: determine representation (let algo guide this choice)
	// for now as int array purely for memory efficiency
	int[] asArray();

	List<Agent> asList();

	static GroupPreference none()
	{
		return GroupPreference.None.instance;
	}

	class None implements GroupPreference
	{
		private static None instance = new None();
		private static int[] asArray = new int[0];

		private None()
		{
		}

		@Override
		public int[] asArray()
		{
			return asArray;
		}

		@Override
		public List<Agent> asList()
		{
			return Collections.emptyList();
		}
	}
}
