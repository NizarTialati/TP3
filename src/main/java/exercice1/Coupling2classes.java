package exercice1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;

import graphs.StaticCallGraph;
import visitors.ClassDeclarationsCollector;

public class Coupling2classes{

	public static void coupling2classes(String projectPath, String classA, String classB) {
		try {
			// path project : /home/tialati/Master_2/Evolution_restructuration/design_patterns/design_patterns/src

			System.out.println("Calculation in progress ...");

			int coupling2classes = Coupling2classes.calculateNumerator(classA, classB, projectPath);
			int allCoupling = Coupling2classes.calculateCoupling(projectPath);

			System.out.println("Number of relation(s) between 2 classes : " + coupling2classes);
			System.out.println("Number of total relation(s) : " + allCoupling);

			double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

			System.out.println("Coupling metric : " + String.format("%.2f", coupling) + " %\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static int calculateNumerator(String classA, String classB, String projectPath) throws IOException {
		StaticCallGraph graphA = StaticCallGraph.createCallGraph(projectPath, classA);
		StaticCallGraph graphB = StaticCallGraph.createCallGraph(projectPath, classB);

		int res = 0;
		for (Map<String, Integer> invocation : graphA.getInvocations().values()) {

			for (String calledMethodName : invocation.keySet()) {
				
				if (graphB.getNodes().stream().filter(m -> m.equals(calledMethodName)).collect(Collectors.toList()).size() > 0 ) {
					res += invocation.get(calledMethodName);
				}
			}
		}

		for (Map<String, Integer> invocation : graphB.getInvocations().values()) {

			for (String calledMethodName : invocation.keySet()) {

				if (graphA.getNodes().stream().filter(m -> m.equals(calledMethodName)).collect(Collectors.toList()).size() > 0 ) {
					
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
	public static List<String> getClasses(String projectPath) {
		try {
			List<String> allClasses = new ArrayList<String>();

			StaticCallGraph graph = new StaticCallGraph(projectPath);

			for (CompilationUnit cUnit : graph.getParser().parseProject()) {

				ClassDeclarationsCollector classCollector = new ClassDeclarationsCollector();
				cUnit.accept(classCollector);

				allClasses.addAll(classCollector.getClasses().stream().map(c -> c.getName().toString())
						.collect(Collectors.toList()));
			}

			return allClasses;
		} catch (IOException e) {

			System.out.println("Une erreur est survenue.");
			return null;
		}
	}

	public static int calculateCoupling(String projectPath) throws IOException {
		
		int res = 0;
		for (String a : getClasses(projectPath)) {
			for (String b : getClasses(projectPath)) {
				if (!a.equals(b)) {
					res += calculateNumerator(a, b, projectPath);
				}
			}
		}
		return res / 2;
	}
}
