package exercice2;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import exercice2.Models.IGroupElement;

public class ModulingClasses {

	public static void moduling(String projectPath) {
		IGroupElement cluster = ClusteringClasses.createClusters(projectPath);

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

			case 1:

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
		
		System.out.print("\n");
	}

}