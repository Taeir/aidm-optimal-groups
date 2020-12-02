package nl.tudelft.aidm.optimalgroups.algorithm.treedecomp;

import jdrasil.algorithms.ExactDecomposer;
import jdrasil.graph.GraphFactory;
import jdrasil.graph.TreeDecomposer;
import jdrasil.graph.TreeDecomposition;
import jdrasil.utilities.logging.JdrasilLogger;
import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.dataset.DatasetContext;
import nl.tudelft.aidm.optimalgroups.model.graph.BipartitieAgentsProjectGraph;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JdrasilTest
{

	public static void main(String[] args) throws Exception
	{
		DatasetContext datasetContext = CourseEdition.fromLocalBepSysDbSnapshot(10);

		BipartitieAgentsProjectGraph datasetAsGraph = BipartitieAgentsProjectGraph.from(datasetContext);

		jdrasil.graph.Graph<Integer> jdrasilGraph = GraphFactory.emptyGraph();


		Logger.getLogger(JdrasilLogger.getName()).setLevel(Level.INFO);

		for (var vert : datasetAsGraph.vertices()) {
//			Objects.requireNonNull(vert);
//			jdrasilGraph.addVertex(vert.id());
		}

		for (var edge : datasetAsGraph.edges().all()) {
//			Objects.requireNonNull(edge);
//			jdrasilGraph.addDirectedEdge(edge.v().id(), edge.w().id());
			if (edge.rank() <= 5) {
				// Bug in jdrasilGraph: must add the vertexes first anyway
				jdrasilGraph.addVertex(edge.v().id());
				jdrasilGraph.addVertex(edge.w().id());

				jdrasilGraph.addEdge(edge.v().id(), edge.w().id());
			}
		}

		TreeDecomposition<Integer> td;
		TreeDecomposer<Integer> decomposer = new ExactDecomposer<>(jdrasilGraph);
		td = decomposer.call();

//		var isValid = td.isValid();

		td.getWidth();


	}

}
