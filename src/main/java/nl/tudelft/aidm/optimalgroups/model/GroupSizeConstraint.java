package nl.tudelft.aidm.optimalgroups.model;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.List;

public interface GroupSizeConstraint
{
	int minSize();
	int maxSize();

}
