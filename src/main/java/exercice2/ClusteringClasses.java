package exercice2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import exercice1.Coupling2classes;
import exercice2.Models.Cluster;
import exercice2.Models.ClusteringClass;
import exercice2.Models.IGroupElement;

public class ClusteringClasses {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		System.out.println("Enter project path : ");

		String projectPath = sc.nextLine();

		sc.close();
		
		createClusters(projectPath);
		
	}
	
	/**
	 * Récupère les classes sous forme d'éléments de couplage.
	 * @param projectPath Le chemin vers le projet.
	 * @return Les éléments de couplage.
	 */
	private static List<IGroupElement> getIGroupElements(String projectPath) {
		
		List<IGroupElement> groups = new ArrayList<>();
		List<String> classNames = Coupling2classes.getClasses(projectPath);
		
		for (String className : classNames) {
			
			groups.add(new ClusteringClass(className));
		}
		
		return groups;
	}
	
	
	private static List<IGroupElement> getCoupling(String projectPath, List<IGroupElement> clusters) {
		
		List<IGroupElement> couplingList = new ArrayList<>();

		try {
			
			
			List<IGroupElement> observedElement = new ArrayList<>();

			int allCoupling = Coupling2classes.calculateCoupling(projectPath);

			for (IGroupElement groupA : clusters) {
				for (IGroupElement groupB : clusters) {

					if (!groupA.equals(groupB) && !observedElement.contains(groupB)) {
						
						double coupling = 0;
						
						for (String classNameA : groupA.getClasses()) {
							
							for (String classNameB : groupB.getClasses()) {
								
								int coupling2classes = Coupling2classes.calculateNumerator(classNameA, classNameB, projectPath);
								
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
	 * Crée la hiérarchie de couplages.
	 * @param projectPath Le chamin vers le projet.
	 * @return Le regroupement.
	 */
	public static IGroupElement createClusters(String projectPath) {

		List<IGroupElement> groupElements = getIGroupElements(projectPath);
		
		List<IGroupElement> possibleClusters = getCoupling(projectPath, groupElements);
		
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
			possibleClusters = getCoupling(projectPath, groupElements);
		}
		
		return groupElements.get(0);
	}
	
}