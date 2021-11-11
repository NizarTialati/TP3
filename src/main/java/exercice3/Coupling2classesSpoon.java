package exercice3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import graphs.StaticCallGraph;
import main.AbstractMain;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

public class Coupling2classesSpoon extends AbstractMain {

	public static void main(String[] args) {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter project path : ");

			String projectPath = sc.nextLine();

			System.out.println("Enter the name of first class : ");

			String classA = sc.nextLine();

			System.out.println("Enter the name of second class : ");

			String classB = sc.nextLine();
			sc.close();
			
			System.out.println("Calculation in progress ...");
			
			// Creation de Spoon
			Launcher spoon = new Launcher();
			spoon.addInputResource(projectPath);

			CtModel model = spoon.buildModel();


			int coupling2classes = calculateNumerator(classA, classB, model);
			int allCoupling = calculateCoupling(model);

			System.out.println("Number of relations between "+classA+" and "+classB +" : " + coupling2classes);
			System.out.println("Number of total relation(s) : " + allCoupling);

			double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

			System.out.println("Coupling metric : " + String.format("%.2f", coupling) + " %");

			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	private static StaticCallGraph createCallGraphSpoon(String classA, CtModel model) {

		StaticCallGraph graph = new StaticCallGraph();

		for (CtType<?> c : model.getAllTypes()) {
			StaticCallGraph partial = new StaticCallGraph();

//			CtType<?> aClass = model.getAllTypes().stream()
//					.filter(cl -> cl.getReference().getSimpleName().equals(classA)).findFirst().orElse(null);

			if (c.getReference().getSimpleName().equals(classA)) {

				for (CtMethod<?> m : c.getAllMethods()) {
					partial.addMethodAndInvocationsSpoon(c, m);
				}

				graph.addMethods(partial.getMethods());
				graph.addInvocations(partial.getInvocations());
			}

		}

		return graph;
	}

	@Override
	protected void menu() {

	}

	public static int calculateNumerator(String classA, String classB, CtModel model) throws IOException {
		StaticCallGraph graphA = createCallGraphSpoon(classA, model);
		StaticCallGraph graphB = createCallGraphSpoon(classB, model);

		int res = 0;
		for (Map<String, Integer> invocation : graphA.getInvocations().values()) {

			for (String calledMethodName : invocation.keySet()) {
				if (calledMethodName.contains(classB)) {
					if (graphB.getMethods().stream().filter(m -> m.contains("::")).collect(Collectors.toList())
							.contains(calledMethodName)) {
						res += invocation.get(calledMethodName);
					}
				}

			}
		}

		for (Map<String, Integer> invocation : graphB.getInvocations().values()) {

			for (String calledMethodName : invocation.keySet()) {
				if (calledMethodName.contains(classA)) {
					if (graphA.getMethods().stream().filter(m -> m.contains("::")).collect(Collectors.toList())
							.contains(calledMethodName)) {
						res += invocation.get(calledMethodName);
					}
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
