package exercice1;

import java.io.IOException;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import graphs.StaticCallGraph;
import utility.Utility;

/**
 * Représente le couplage entre 2 classes d'une application.
 */
public class Coupling2classes {

	/**
	 * Calcul la métrique du couplage entre 2 classes données. (Exercice 1 - Question1)
	 * @param projectPath Le chemin du projet
	 * @param classA Le nom de la classe A.
	 * @param classB Le nom de la classe B.
	 */
	public static void coupling2classes(String projectPath, String classA, String classB) {
		try {
			// path project :
			// /home/tialati/Master_2/Evolution_restructuration/design_patterns/design_patterns/src

			StaticCallGraph graph = StaticCallGraph.createCallGraph(projectPath);


			int coupling2classes = Coupling2classes.calculateNumerator(classA, classB, graph);
			int allCoupling = Coupling2classes.calculateCoupling(graph);

			System.out.println("Number of relation(s) between 2 classes : " + coupling2classes);
			System.out.println("Number of total relation(s) : " + allCoupling);

			double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

			System.out.println("Coupling metric : " + String.format("%.3f", coupling) + " %\n\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	
	/**
	 * Calcule le nombre de relations entre deux classes données.
	 * @param classA Nom de la classe A
	 * @param classB Nom de la classe B
	 * @param graph Graphe d'appel
	 * @return Le nombre de relation
	 * @throws IOException
	 */
	public static int calculateNumerator(String classA, String classB, StaticCallGraph graph) throws IOException {

		int res = 0;
		// On parcourt les methodes declarés par la classe A
		for (String m : graph.getInvocations().keySet().stream().filter(t -> {
			String s = t.split("::")[0];
			return s.contains("."+classA) || s.equals(classA);
		}).collect(Collectors.toList())) {

			// On parcourt les methodes invoquées par la classe A
			for (String invokedMethod : graph.getInvocations().get(m).keySet().stream().filter(t -> {
				String s = t.split("::")[0];
				return s.contains("."+classB) || s.equals(classB);
			}).collect(Collectors.toList())) {

				if (invokedMethod != null) {
					res += graph.getInvocations().get(m).get(invokedMethod);
				}
			}
		}

		// On parcourt les methodes declarés par la classe B
		for (String m : graph.getInvocations().keySet().stream().filter(t -> {
			String s = t.split("::")[0];
			return s.contains("."+classB) || s.equals(classB);
		}).collect(Collectors.toList())) {

			// On parcourt les methodes invoquées par la classe B
			for (String invokedMethod : graph.getInvocations().get(m).keySet().stream().filter(t -> {
				String s = t.split("::")[0];
				return s.contains("."+classA) || s.equals(classA);
			}).collect(Collectors.toList())) {

				if (invokedMethod != null) {
					res += graph.getInvocations().get(m).get(invokedMethod);
				}
			}
		}
		
		return res;
	}

	/**
	 * Calcule le nombre de relations entre toutes les classes.
	 * @param graph Graphe d'appel
	 * @return Le nombre de relation
	 * @throws IOException
	 */
	public static int calculateCoupling(StaticCallGraph graph) throws IOException {

		int res = 0;
		for (TypeDeclaration a : graph.getClasses()) {
			
			String classA = Utility.getClassFullyQualifiedName(a);
			for (TypeDeclaration b : graph.getClasses()) {
				
				String classB = Utility.getClassFullyQualifiedName(b);
				if (!classA.equals(classB)) {
					res += calculateNumerator(classA, classB, graph);
				}
			}
		}

		return res / 2;
	}
}
