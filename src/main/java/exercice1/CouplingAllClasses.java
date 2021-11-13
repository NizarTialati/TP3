package exercice1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CouplingAllClasses {

	public static void couplingAllClasses(String projectPath) {
		

		try {
			
			System.out.println("Calculation in progress ...");
			
			List<String> classNames = Coupling2classes.getClasses(projectPath);
			List<String> observedClasses = new ArrayList<>();

			int allCoupling = Coupling2classes.calculateCoupling(projectPath);

			for (String classNameA : classNames) {
				for (String classNameB : classNames) {

					if (!classNameA.equals(classNameB) && !observedClasses.contains(classNameB)) {

						int coupling2classes = Coupling2classes.calculateNumerator(classNameA, classNameB, projectPath);

						double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

						System.out.println("Coupling metric between " + classNameA + " and " + classNameB + " : "
								+ String.format("%.2f", coupling) + " %");
					}
				}
				
				observedClasses.add(classNameA);
			}
			
			System.out.print("\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
