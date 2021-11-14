package exercice2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import exercice1.Coupling2classes;
import exercice2.Models.Cluster;
import exercice2.Models.ClusteringClass;
import exercice2.Models.IGroupElement;
import graphs.StaticCallGraph;
import utility.Utility;

public class ClusteringClasses {

	/**
	 * Récupère les classes sous forme d'éléments de couplage.
	 * 
	 * @param graph Le graphe d'appel.
	 * @return Les éléments de couplage.
	 * @throws IOException
	 */
	private static List<IGroupElement> getIGroupElements(StaticCallGraph graph) {

		List<IGroupElement> groups = new ArrayList<>();

		List<String> classNames = new ArrayList<>();

		classNames = graph.getClasses().stream().map(s -> Utility.getClassFullyQualifiedName(s))
				.collect(Collectors.toList());

		for (String className : classNames) {

			groups.add(new ClusteringClass(className));
		}

		return groups;
	}

	private static List<IGroupElement> getCoupling(List<IGroupElement> clusters, StaticCallGraph graph) {

		List<IGroupElement> couplingList = new ArrayList<>();

		try {

			List<IGroupElement> observedElement = new ArrayList<>();

			int allCoupling = Coupling2classes.calculateCoupling(graph);

			for (IGroupElement groupA : clusters) {
				for (IGroupElement groupB : clusters) {

					if (!groupA.equals(groupB) && !observedElement.contains(groupB)) {

						double coupling = 0;

						for (String classNameA : groupA.getClasses()) {

							for (String classNameB : groupB.getClasses()) {

								int coupling2classes = Coupling2classes.calculateNumerator(classNameA, classNameB,
										graph);

								coupling += (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;
							}
						}

						Cluster tempCluster = new Cluster(groupA, groupB, coupling);
						couplingList.add(tempCluster);
					}
				}

				observedElement.add(groupA);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return couplingList;
	}

	/**
	 * Récupère le meilleur couplage.
	 * 
	 * @param clusters Tous les couplages possibles.
	 * @return Le meilleur couplage.
	 */
	private static IGroupElement getBestCoupling(List<IGroupElement> clusters) {

		if (clusters.isEmpty()) {
			return null;
		}

		double maxScore = clusters.stream().mapToDouble(c -> c.getScore()).max().orElse(0.0);

		return clusters.stream().filter(c -> c.getScore() == maxScore).findFirst().orElse(null);
	}

	/**
	 * Crée la hiérarchie de couplages (Exercice 2 - Question 1).
	 * @param projectPath Le chamin vers le projet.
	 * @return Le regroupement.
	 * @throws IOException
	 */
	public static IGroupElement createClusters(String projectPath) throws IOException {

		StaticCallGraph graph = StaticCallGraph.createCallGraph(projectPath);

		List<IGroupElement> groupElements = getIGroupElements(graph);

		List<IGroupElement> possibleClusters = getCoupling(groupElements, graph);

		while (groupElements.size() > 1) {

			IGroupElement bestCouple = getBestCoupling(possibleClusters);
			List<IGroupElement> clusterToRemove = new ArrayList<>();

			for (IGroupElement couple : groupElements) {

				for (String className : couple.getClasses()) {

					if (bestCouple.getClasses().contains(className)) {
						clusterToRemove.add(couple);
					}
				}
			}

			groupElements.removeAll(clusterToRemove);
			groupElements.add(bestCouple);
			System.out.println("Cluster : " + bestCouple.toString());
			possibleClusters = getCoupling(groupElements, graph);
		}

		System.out.println("\n");
		return groupElements.get(0);
	}

}