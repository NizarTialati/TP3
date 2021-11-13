package exercice3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import graphs.StaticCallGraph;
import spoon.Launcher;
import spoon.reflect.CtModel;

public class Coupling2classesSpoon {

	public static void coupling2classesSpoon(String projectPath, String classA, String classB) {
		try {

			System.out.println("Calculation in progress ...");

			// Creation de Spoon
			Launcher spoon = new Launcher();
			spoon.addInputResource(projectPath);
			
			CtModel model = spoon.buildModel();

			int coupling2classes = calculateNumerator(classA, classB, model);
			int allCoupling = calculateCoupling(model);

			System.out.println("Number of relations between " + classA + " and " + classB + " : " + coupling2classes);
			System.out.println("Number of total relation(s) : " + allCoupling);

			double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

			System.out.println("Coupling metric : " + String.format("%.2f", coupling) + " %");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static int calculateNumerator(String classA, String classB, CtModel model) throws IOException {
		StaticCallGraph graphA = StaticCallGraph.createCallGraphSpoon(classA, model);
		StaticCallGraph graphB = StaticCallGraph.createCallGraphSpoon(classB, model);
		
		
		int res = 0;
		for (Map<String, Integer> invocation : graphA.getInvocations().values()) {

			for (String calledMethodName : invocation.keySet()) {
				if (graphB.getNodes().stream().filter(m -> m.equals(calledMethodName)).collect(Collectors.toList()).size() > 0) {
					res += invocation.get(calledMethodName);
				}

			}
		}

		for (Map<String, Integer> invocation : graphB.getInvocations().values()) {

			for (String calledMethodName : invocation.keySet()) {
				if (graphA.getNodes().stream().filter(m -> m.equals(calledMethodName)).collect(Collectors.toList()).size() > 0) {
					res += invocation.get(calledMethodName);
				}

			}
		}
		
		return res;
	}

	/**
	 * Récupère les classes de l'application analysée.
	 * 
	 * @return La liste des noms des classes de l'application.
	 */
	public static List<String> getClassesSpoon(CtModel model) {

		List<String> allClasses = new ArrayList<String>();

		allClasses.addAll(
				model.getAllTypes().stream().map(c -> c.getReference().getSimpleName()).collect(Collectors.toList()));

		return allClasses;

	}

	public static int calculateCoupling(CtModel model) throws IOException {
		int res = 0;
		for (String a : getClassesSpoon(model)) {
			for (String b : getClassesSpoon(model)) {
				if (!a.equals(b)) {
					res += calculateNumerator(a, b, model);
				}
			}
		}
		return res / 2;
	}
}
