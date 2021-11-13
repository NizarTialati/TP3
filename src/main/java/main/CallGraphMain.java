package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import exercice1.Coupling2classes;
import exercice1.CouplingAllClasses;
import exercice2.ClusteringClasses;
import exercice2.ModulingClasses;
import exercice3.ClusteringClassesSpoon;
import exercice3.Coupling2classesSpoon;
import exercice3.CouplingAllClassesSpoon;
import exercice3.ModulingClassesSpoon;
import graphs.CallGraph;
import graphs.StaticCallGraph;
import processors.ASTProcessor;

public class CallGraphMain extends AbstractMain {

	@Override
	protected void menu() {
		StringBuilder builder = new StringBuilder();
		builder.append("1. Static call graph.");
		builder.append("\n2. Coupling 2 classes.");
		builder.append("\n3. Coupling all classes.");
		builder.append("\n4. Clustering.");
		builder.append("\n5. Moduling.");
		builder.append("\n6. Coupling 2 classes with Spoon.");
		builder.append("\n7. Coupling all classes with Spoon.");
		builder.append("\n8. Clustering with Spoon.");
		builder.append("\n9. Moduling with Spoon.");
		builder.append("\n" + QUIT + ". To quit.");

		System.out.println(builder);
	}

	public static void main(String[] args) {
		CallGraphMain main = new CallGraphMain();
		BufferedReader inputReader;
		CallGraph callGraph = null;
		try {
			inputReader = new BufferedReader(new InputStreamReader(System.in));
			if (args.length < 1)
				setTestProjectPath(inputReader);
			else
				verifyTestProjectPath(inputReader, args[0]);
			String userInput = "";

			do {
				main.menu();
				userInput = inputReader.readLine();
				main.processUserInput(userInput, callGraph);
				Thread.sleep(3000);

			} while (!userInput.equals(QUIT));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void processUserInput(String userInput, ASTProcessor processor) {
		CallGraph callGraph = (CallGraph) processor;
		Scanner sc = new Scanner(System.in);
		String classA, classB;
		try {
			switch (userInput) {
			case "1":
				callGraph = StaticCallGraph.createCallGraph(TEST_PROJECT_PATH);
				callGraph.log();
				break;

			case "2":
				
				System.out.println("Enter the name of first class : ");
				classA = sc.nextLine();
				System.out.println("Enter the name of second class : ");
				classB = sc.nextLine();
				
				System.out.println("Start Coupling...\n");
				Coupling2classes.coupling2classes(TEST_PROJECT_PATH, classA, classB);
				
				break;

			case "3":
				System.out.println("Start Coupling...\n");
				CouplingAllClasses.couplingAllClasses(TEST_PROJECT_PATH);
				break;
				
			case "4":
				System.out.println("Start Clustering...\n");
				ClusteringClasses.createClusters(TEST_PROJECT_PATH);
				break;
				
			case "5":
				System.out.println("\nStart moduling ... \n");
				ModulingClasses.moduling(TEST_PROJECT_PATH);
				break;
			
			case "6":
				System.out.println("Enter the name of first class : ");
				classA = sc.nextLine();
				System.out.println("Enter the name of second class : ");
				classB = sc.nextLine();
				
				Coupling2classesSpoon.coupling2classesSpoon(TEST_PROJECT_PATH, classA, classB);
				break;
			
			case "7":
				System.out.println("\nStart Coupling ... \n");
				CouplingAllClassesSpoon.couplingAllClassesSpoon(TEST_PROJECT_PATH);
				break;
				
			case "8":
				System.out.println("\nStart Clustering ... \n");
				ClusteringClassesSpoon.createClusters(TEST_PROJECT_PATH);
				break;
			
			case "9":
				System.out.println("\nStart moduling ... \n");
				ModulingClassesSpoon.modulingClassesSpoon(TEST_PROJECT_PATH);
				break;
				
			case QUIT:
				System.out.println("Bye...");
				sc.close();
				return;

			default:
				System.err.println("Sorry, wrong input. Please try again.");
				sc.close();
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
