package exercice3;

import java.io.IOException;

import exercice1.Coupling2classes;
import graphs.StaticCallGraph;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;

public class Coupling2classesSpoon {

	public static void coupling2classesSpoon(String projectPath, String classA, String classB) {
		
		try {

			System.out.println("Calculation in progress ...");

			// Creation de Spoon
			Launcher spoon = new Launcher();
			spoon.addInputResource(projectPath);
			
			CtModel model = spoon.buildModel();
			
			StaticCallGraph graph = StaticCallGraph.createCallGraphSpoon(model);
			
			int coupling2classes = Coupling2classes.calculateNumerator(classA, classB, graph);
			int allCoupling = calculateCouplingSpoon(graph);

			System.out.println("Number of relations between " + classA + " and " + classB + " : " + coupling2classes);
			System.out.println("Number of total relation(s) : " + allCoupling);

			double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

			System.out.println("Coupling metric : " + String.format("%.3f", coupling) + " %\n\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Calcule toutes les relations entre les classes avec Spoon.
	 * @param graph Graphe d'appel
	 * @return Le nombre de relation total.
	 * @throws IOException
	 */
	public static int calculateCouplingSpoon(StaticCallGraph graph) throws IOException {
		int res = 0;
		for (CtClass<?> a : graph.getClassesSpoon()) {
			
			String classA = a.getQualifiedName();
			for (CtClass<?> b : graph.getClassesSpoon()) {
				
				String classB = b.getQualifiedName();
				if (!classA.equals(classB)) {
					res += Coupling2classes.calculateNumerator(classA, classB, graph);
				}
			}
		}

		return res / 2;
	}
}
