package exercice2.Models;

import java.util.List;

/**
 * Représente un élément de couple.
 */
public interface IGroupElement {

	/**
	 * Récupère les classes associées.
	 * @return Les classes associées.
	 */
	public List<String> getClasses();
	
	/**
	 * Récupère les éléments associés.
	 * @return Les éléments associés.
	 */
	public List<IGroupElement> getElements();
	
	/**
	 * Récupère le score.
	 * @return un score.
	 */
	public double getScore();
}