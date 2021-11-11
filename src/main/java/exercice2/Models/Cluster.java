package exercice2.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un couple de classes.
 */
public class Cluster implements IGroupElement {
	
	/**
	 * Les noms des classes regroupées.
	 */
	private List<IGroupElement> classes = new ArrayList<>();
	
	/**
	 * Le score.
	 */
	private double score;
	
	/**
	 * Constructeur.
	 * @param groupA Premier élément du couple.
	 * @param groupB Second élément du couple.
	 * @param coupling Le score.
	 */
	public Cluster(IGroupElement groupA, IGroupElement groupB, double coupling) {
		
		classes.add(groupA);
		classes.add(groupB);
		
		score = coupling;
	}

	@Override
	public List<String> getClasses() {
		
		List<String> classNames = new ArrayList<>();
		
		for (IGroupElement group : classes) {
			
			classNames.addAll(group.getClasses());
		}
		
		return classNames;
	}
	
	/**
	 * Récupère le score.
	 * @return Le score.
	 */
	public double getScore() {
		
		return this.score;
	}

	@Override
	public List<IGroupElement> getElements() {

		return this.classes;
	}
	
	@Override
	public String toString() {

		String result = "\n\tScore : " + this.score;
		
		for (IGroupElement element : classes) {
			
			if (element instanceof ClusteringClass) {
				
				result += element.toString();
			}
		}
		
		return result;
	}
}