package exercice1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import graphs.StaticCallGraph;
import main.AbstractMain;
import visitors.ClassDeclarationsCollector;
import visitors.MethodDeclarationsCollector;

public class Coupling2classes extends AbstractMain {

	public static void main(String[] args) {

		// path projet : /home/tialati/Master_2/Evolution_restructuration/TP3/src/
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

			int coupling2classes = calculateNumerator(classA, classB, projectPath);
			int allCoupling = calculateCoupling(projectPath);

			System.out.println("Number of relation(s) between 2 classes : " + coupling2classes);
			System.out.println("Number of total relation(s) : " + allCoupling);

			double coupling = (Double.valueOf(coupling2classes) / Double.valueOf(allCoupling)) * 100;

			System.out.println("Coupling metric : " + String.format("%.2f", coupling) + " %");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static StaticCallGraph createCallGraph(String projectPath, String classname) throws IOException {

		StaticCallGraph graph = new StaticCallGraph(projectPath);

		for (CompilationUnit cUnit : graph.getParser().parseProject()) {

			StaticCallGraph partial = new StaticCallGraph(projectPath);
			ClassDeclarationsCollector classCollector = new ClassDeclarationsCollector();
			cUnit.accept(classCollector);

			TypeDeclaration aClass = classCollector.getClasses().stream()
					.filter(c -> c.getName().toString().equals(classname)).findFirst().orElse(null);

			if (aClass != null) {

				MethodDeclarationsCollector methodCollector = new MethodDeclarationsCollector();
				aClass.accept(methodCollector);

				for (MethodDeclaration method : methodCollector.getMethods()) {
					partial.addMethodAndInvocations(aClass, method);

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

	public static int calculateNumerator(String classA, String classB, String projectPath) throws IOException {
		StaticCallGraph graphA = createCallGraph(projectPath, classA);
		StaticCallGraph graphB = createCallGraph(projectPath, classB);

		int res = 0;
		for (Map<String, Integer> invocation : graphA.getInvocations().values()) {

			for (String calledMethodName : invocation.keySet()) {

				if (graphB.getMethods().stream().filter(m -> m.contains("::")).collect(Collectors.toList())
						.contains(classB + "::" + calledMethodName)) {
					res += invocation.get(calledMethodName);
				}
			}
		}

		for (Map<String, Integer> invocation : graphB.getInvocations().values()) {

			for (String calledMethodName : invocation.keySet()) {

				if (graphA.getMethods().stream().filter(m -> m.contains("::")).collect(Collectors.toList())
						.contains(classA + "::" + calledMethodName)) {
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
