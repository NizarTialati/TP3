package exercice3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import spoon.Launcher;
import spoon.reflect.CtModel;

public class CouplingAllClassesSpoon {

	public static void main(String[] args) {
		

		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter project path : ");

			String projectPath = sc.nextLine();
			sc.close();
			
			System.out.println("Calculation in progress ...");
			
			// Creation de Spoon
			Launcher spoon = new Launcher();
			spoon.addInputResource(projectPath);

			CtModel model = spoon.buildModel();
			

			List<String> classNames = Coupling2classesSpoon.getClassesSpoon(model);
			List<String> observedClasses = new ArrayList<>();

			int allCoupling = Coupling2classesSpoon.calculateCoupling(model);

			for (String classNameA : classNames) {
				for (String classNameB : classNames) {

					if (!classNameA.equals(classNameB) && !observedClasses.contains(classNameB)) {

						int coupling2classes = Coupling2classesSpoon.calculateNumerator(classNameA, classNameB, model);

						double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

						System.out.println("Coupling metric between " + classNameA + " and " + classNameB + " : "
								+ String.format("%.2f", coupling) + " %");
					}
				}
				
				observedClasses.add(classNameA);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
