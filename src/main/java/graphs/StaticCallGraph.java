package graphs;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;
import utility.Utility;
import visitors.ClassDeclarationsCollector;
import visitors.MethodDeclarationsCollector;
import visitors.MethodInvocationsCollector;

public class StaticCallGraph extends CallGraph {

	/* CONSTRUCTORS */
	public StaticCallGraph(String projectPath) {
		super(projectPath);
	}

	public StaticCallGraph() {
		super(null);
	}

	/* METHODS */
	public static StaticCallGraph createCallGraph(String projectPath, CompilationUnit cUnit) {
		StaticCallGraph graph = new StaticCallGraph(projectPath);
		ClassDeclarationsCollector classCollector = new ClassDeclarationsCollector();
		cUnit.accept(classCollector);

		for (TypeDeclaration cls : classCollector.getClasses()) {
			graph.addClass(cls);
			MethodDeclarationsCollector methodCollector = new MethodDeclarationsCollector();
			cls.accept(methodCollector);

			for (MethodDeclaration method : methodCollector.getMethods())
				graph.addInvocations(cls, method);
		}

		return graph;
	}

	public static StaticCallGraph createCallGraph(String projectPath) throws IOException {
		StaticCallGraph graph = new StaticCallGraph(projectPath);

		for (CompilationUnit cUnit : graph.parser.parseProject()) {
			StaticCallGraph partial = StaticCallGraph.createCallGraph(projectPath, cUnit);
			graph.addClasses(partial.getClasses());
			graph.addNodes(partial.getNodes());
			graph.addInvocations(partial.getInvocations());
			graph.addMethodDeclarationsMappings(partial.getMethodDeclarationsMap());
		}

		return graph;
	}

	public static StaticCallGraph createCallGraph(String projectPath, String className) throws IOException {
		StaticCallGraph graph = new StaticCallGraph(projectPath);

		for (CompilationUnit cUnit : graph.parser.parseProject()) {

			StaticCallGraph partial = new StaticCallGraph(projectPath);
			ClassDeclarationsCollector classCollector = new ClassDeclarationsCollector();
			cUnit.accept(classCollector);

			TypeDeclaration aClass = classCollector.getClasses().stream()
					.filter(c -> c.getName().toString().equals(className)).findFirst().orElse(null);

			if (aClass != null) {
				partial.addClass(aClass);

				MethodDeclarationsCollector methodCollector = new MethodDeclarationsCollector();
				aClass.accept(methodCollector);

				for (MethodDeclaration method : methodCollector.getMethods()) {

					partial.addInvocations(aClass, method);

				}

				graph.addClasses(partial.getClasses());
				graph.addNodes(partial.getNodes());
				graph.addInvocations(partial.getInvocations());
				graph.addMethodDeclarationsMappings(partial.getMethodDeclarationsMap());
			}
		}

		return graph;
	}

	public static StaticCallGraph createCallGraphSpoon(String classA, CtModel model) {

		StaticCallGraph graph = new StaticCallGraph();

		StaticCallGraph partial = new StaticCallGraph();

		CtType<?> aClass = model.getAllTypes().stream().filter(cl -> cl.getReference().getSimpleName().equals(classA))
				.findFirst().orElse(null);

		if (aClass != null) {

			for (CtMethod<?> m : aClass.getMethods()) {

				partial.addMethodAndInvocationsSpoon(aClass, m);
			}

			graph.addNodes(partial.getNodes());
			graph.addInvocations(partial.getInvocations());

		}

		return graph;
	}

	public boolean addMethodAndInvocationsSpoon(CtType<?> cls, CtMethod<?> method) {

		if (method.getBody() != null) {
			String methodName = cls.getReference().getQualifiedName() + "::" + method.getSimpleName();

			this.addNode(methodName);
			List<CtInvocation<?>> invocationCollector = method.getElements(new TypeFilter<>(CtInvocation.class));
			this.addInvocationsSpoon(cls, method, methodName, invocationCollector);

//			for (CtInvocation<?> i : invocationCollector) {
//				List<CtSuperAccess<?>> superInvocations = i.getElements(new TypeFilter<>(CtSuperAccess.class));
//
//				this.addSuperInvocationsSpoon(methodName, superInvocations);
//			}

		}

		return method.getBody() != null;

	}

	private void addInvocationsSpoon(CtType<?> cls, CtMethod<?> method, String methodName,
			List<CtInvocation<?>> invocationCollector) {

		for (CtInvocation<?> invocation : invocationCollector) {

			String invocationName = getMethodInvocationNameSpoon(cls, invocation);

			this.addInvocationSpoon(methodName, invocationName);
		}
	}

	private String getMethodInvocationNameSpoon(CtType<?> cls, CtInvocation<?> invocation) {
		String invocationName = "";

		CtExecutable<?> invocationNameClass = invocation.getExecutable().getExecutableDeclaration();

		if (invocationNameClass != null) {

			invocationName += invocationNameClass.getReference().getDeclaringType().getQualifiedName() + "::";
			invocationName += invocationNameClass.getReference().getSimpleName();
		}

		return invocationName;
	}

	private void addSuperInvocationsSpoon(String methodName, List<CtSuperAccess<?>> invocationCollector) {
		for (CtSuperAccess<?> superInvocation : invocationCollector) {
			String superInvocationName = superInvocation.toString();
			this.addNode(superInvocationName);
			this.addInvocationSpoon(methodName, superInvocationName);
		}
	}

	private boolean addInvocations(TypeDeclaration cls, MethodDeclaration methodDeclaration) {
		if (methodDeclaration.getBody() != null) {
			MethodInvocationsCollector invocationCollector = new MethodInvocationsCollector();

			this.addNode(Utility.getMethodFullyQualifiedName(methodDeclaration));
			this.addInvocations(cls, methodDeclaration, invocationCollector);
			this.addSuperInvocations(methodDeclaration, invocationCollector);

		}

		return methodDeclaration.getBody() != null;
	}

	private void addInvocations(TypeDeclaration cls, MethodDeclaration method,
			MethodInvocationsCollector invocationCollector) {
		method.accept(invocationCollector);
		for (MethodInvocation invocation : invocationCollector.getMethodInvocations()) {

			this.addInvocation(method, invocation);
		}

	}

	private void addSuperInvocations(MethodDeclaration method, MethodInvocationsCollector invocationCollector) {
		for (SuperMethodInvocation superInvocation : invocationCollector.getSuperMethodInvocations())
			this.addInvocation(method, superInvocation);
	}
}