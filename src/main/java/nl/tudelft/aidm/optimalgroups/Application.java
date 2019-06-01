package nl.tudelft.aidm.optimalgroups;

import nl.tudelft.aidm.optimalgroups.algorithm.group.BepSysWithRandomGroups;
import nl.tudelft.aidm.optimalgroups.model.entity.Agents;
import nl.tudelft.aidm.optimalgroups.model.entity.Group;
import org.sql2o.GenericDatasource;

import javax.sql.DataSource;
import java.util.Collection;

public class Application
{
	public static void main(String[] args)
	{
		DataSource dataSource = new GenericDatasource("jdbc:mysql://localhost:3306/test", "henk", "henk");

		Agents agents = Agents.from(dataSource, 10);
		BepSysWithRandomGroups formedGroups = new BepSysWithRandomGroups(agents, 4, 6);

		Collection<Group> groups = formedGroups.asCollection();
	}
}
