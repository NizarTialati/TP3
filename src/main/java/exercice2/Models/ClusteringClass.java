package exercice2.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une classe.
 */
public class ClusteringClass implements IGroupElement {
	
	/**
	 * La classe déclarée.
	 */
	private String clazz;
	
	/**
	 * Constructeur.
	 * @param c La classe.
	 */
	public ClusteringClass(String c) {
		clazz = c;
	}

	@Override
	public List<String> getClasses() {

		List<String> classes = new ArrayList<>();
		classes.add(clazz);
		
		return classes;
	}

	@Override
	public List<IGroupElement> getElements() {
		
		return new ArrayList<>();
	}
	
	@Override
	public double getScore() {
		return 0;
	}
	
	@Override
	public String toString() {
		
		return " Class : " + this.clazz;
	}
}