package exercice3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import exercice1.Coupling2classes;
import graphs.StaticCallGraph;
import spoon.Launcher;
import spoon.reflect.CtModel;

public class CouplingAllClassesSpoon {

	public static void couplingAllClassesSpoon(String projectPath) {
		
		try {
			
			System.out.println("Calculation in progress ...");
			
			// Creation de Spoon
			Launcher spoon = new Launcher();
			spoon.addInputResource(projectPath);

			CtModel model = spoon.buildModel();
			
			StaticCallGraph graph = StaticCallGraph.createCallGraphSpoon(model);
			
			List<String> classNames = graph.getClassesSpoon().stream().map(c -> c.getQualifiedName()).collect(Collectors.toList());
			List<String> observedClasses = new ArrayList<>();

			int allCoupling = Coupling2classesSpoon.calculateCouplingSpoon(graph);

			for (String classNameA : classNames) {
				for (String classNameB : classNames) {

					if (!classNameA.equals(classNameB) && !observedClasses.contains(classNameB)) {

						int coupling2classes = Coupling2classes.calculateNumerator(classNameA, classNameB, graph);

						double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

						System.out.println("Coupling metric between " + classNameA + " and " + classNameB + " : "
								+ String.format("%.3f", coupling) + " %");
					}
				}
				
				observedClasses.add(classNameA);
			}
			
			System.out.print("\n\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
