package nl.tudelft.aidm.optimalgroups.model.pref;

import nl.tudelft.aidm.optimalgroups.model.agent.Agent;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface GroupPreference
{
	// TODO: determine representation (let algo guide this choice)
	// for now as int array purely for memory efficiency
	int[] asArray();

	List<Agent> asListOfAgents();

	Integer count();

	/* */
	static GroupPreference none()
	{
		return GroupPreference.None.instance;
	}

	/* */
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
		public List<Agent> asListOfAgents()
		{
			return Collections.emptyList();
		}

		@Override
		public Integer count()
		{
			return 0;
		}
	}
}
