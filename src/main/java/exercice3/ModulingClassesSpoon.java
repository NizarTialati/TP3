package exercice3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import exercice2.Models.IGroupElement;
import spoon.Launcher;
import spoon.reflect.CtModel;

public class ModulingClassesSpoon {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		System.out.println("Enter project path : ");

		String projectPath = sc.nextLine();

		sc.close();
		
		// Creation de Spoon
		Launcher spoon = new Launcher();
		spoon.addInputResource(projectPath);

		CtModel model = spoon.buildModel();
		
		IGroupElement cluster = ClusteringClassesSpoon.createClusters(model);

		System.out.println("\nStart moduling ... \n");
		moduling(cluster);

	}

	private static void moduling(IGroupElement cluster) {
		Stack<IGroupElement> stack = new Stack<>();
		stack.push(cluster);

		List<IGroupElement> lsAllClusters = new ArrayList<>();

		while (!stack.isEmpty()) {
			IGroupElement father = stack.pop();
			IGroupElement child1;
			switch (father.getElements().size()) {
			case 2:
				
				child1 = father.getElements().get(0);
				IGroupElement child2 = father.getElements().get(1);
				
				if (father.getScore() > (child1.getScore() + child2.getScore()) / 2) {
					lsAllClusters.add(father);

					System.out.println("Current father cluster : " + father.toString());

				} else {

					stack.push(child1);
					stack.push(child2);
				}
				break;
			
			case 1 :
				
				child1 = father.getElements().get(0);
				
				
				if (father.getScore() > child1.getScore()) {
					lsAllClusters.add(father);

					System.out.println("Current father cluster : " + father.toString());

				} else {

					stack.push(child1);
				}
				
				break;
			default:
				
				break;
			}

			
		}
	}

}