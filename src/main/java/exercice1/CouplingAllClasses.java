package exercice1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import graphs.StaticCallGraph;
import utility.Utility;

/**
 * Représente le couplage de toute l'application.
 */
public class CouplingAllClasses {

	/**
	 * Calcul la métrique du couplage de toute l'application. (Exercice 1 - Question 2).
	 * @param projectPath Le chemin du projet.
	 */
	public static void couplingAllClasses(String projectPath) {

		try {

			StaticCallGraph graph = StaticCallGraph.createCallGraph(projectPath);

			List<String> classNames = graph.getClasses().stream().map(s -> Utility.getClassFullyQualifiedName(s))
					.collect(Collectors.toList());

			List<String> observedClasses = new ArrayList<>();

			int allCoupling = Coupling2classes.calculateCoupling(graph);

			for (String classNameA : classNames) {

				for (String classNameB : classNames) {

					if (!classNameA.equals(classNameB) && !observedClasses.contains(classNameB)) {

						int coupling2classes = Coupling2classes.calculateNumerator(classNameA, classNameB, graph);

						double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

						System.out.println("Coupling metric between " + classNameA + " and " + classNameB + " : "
								+ String.format("%.3f", coupling) + " %");
					}
				}

				observedClasses.add(classNameA);
			}

			System.out.println("\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
