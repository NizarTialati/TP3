package graphs;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;
import visitors.ClassDeclarationsCollector;
import visitors.MethodDeclarationsCollector;
import visitors.MethodInvocationsCollector;

public class StaticCallGraph extends CallGraph {

	/* CONSTRUCTOR */
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
			MethodDeclarationsCollector methodCollector = new MethodDeclarationsCollector();
			cls.accept(methodCollector);

			for (MethodDeclaration method : methodCollector.getMethods())
				graph.addMethodAndInvocations(cls, method);
		}

		return graph;
	}

	public static StaticCallGraph createCallGraph(String projectPath) throws IOException {
		StaticCallGraph graph = new StaticCallGraph(projectPath);

		for (CompilationUnit cUnit : graph.parser.parseProject()) {
			StaticCallGraph partial = StaticCallGraph.createCallGraph(projectPath, cUnit);
			graph.addMethods(partial.getMethods());
			graph.addInvocations(partial.getInvocations());
		}

		return graph;
	}

	public boolean addMethodAndInvocations(TypeDeclaration cls, MethodDeclaration method) {
		if (method.getBody() != null) {
			String methodName = cls.getName().toString() + "::" + method.getName().toString();
			this.addMethod(methodName);
			MethodInvocationsCollector invocationCollector = new MethodInvocationsCollector();
			this.addInvocations(cls, method, methodName, invocationCollector);
			//this.addSuperInvocations(methodName, invocationCollector);
		}

		return method.getBody() != null;
	}

	public boolean addMethodAndInvocationsSpoon(CtType<?> cls, CtMethod<?> method) {

		if (method.getBody() != null) {
			String methodName = cls.getReference().getSimpleName() + "::" + method.getSimpleName();

			this.addMethod(methodName);
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

	private void addInvocations(TypeDeclaration cls, MethodDeclaration method, String methodName,
			MethodInvocationsCollector invocationCollector) {
		method.accept(invocationCollector);

		for (MethodInvocation invocation : invocationCollector.getMethodInvocations()) {
			String invocationName = getMethodInvocationName(cls, invocation);
			this.addMethod(invocationName);
			this.addInvocation(methodName, invocationName);
		}
	}

	private void addInvocationsSpoon(CtType<?> cls, CtMethod<?> method, String methodName,
			List<CtInvocation<?>> invocationCollector) {

		for (CtInvocation<?> invocation : invocationCollector) {

			String invocationName = getMethodInvocationNameSpoon(cls, invocation);
			this.addMethod(invocationName);
			this.addInvocation(methodName, invocationName);
		}
	}

	private String getMethodInvocationName(TypeDeclaration cls, MethodInvocation invocation) {
		String invocationName = "";

		IMethodBinding invocationMethod = invocation.resolveMethodBinding();

		if (invocationMethod != null) {

			invocationName += invocationMethod.getDeclaringClass().getName().toString() + "::";
		}

		invocationName += invocation.getName().toString();

		return invocationName;
	}

	private String getMethodInvocationNameSpoon(CtType<?> cls, CtInvocation<?> invocation) {
		String invocationName = "";

		CtExecutable<?> invocationNameClass = invocation.getExecutable().getExecutableDeclaration();

		if (invocationNameClass != null) {

			invocationName += invocationNameClass.getReference().getDeclaringType().getSimpleName() + "::";
			invocationName += invocationNameClass.getReference().getSimpleName();
		}

		

		return invocationName;
	}

	private void addSuperInvocations(String methodName, MethodInvocationsCollector invocationCollector) {
		for (SuperMethodInvocation superInvocation : invocationCollector.getSuperMethodInvocations()) {
			String superInvocationName = superInvocation.getName().getFullyQualifiedName();
			this.addMethod(superInvocationName);
			this.addInvocation(methodName, superInvocationName);
		}
	}

	private void addSuperInvocationsSpoon(String methodName, List<CtSuperAccess<?>> invocationCollector) {
		for (CtSuperAccess<?> superInvocation : invocationCollector) {
			String superInvocationName = superInvocation.toString();
			this.addMethod(superInvocationName);
			this.addInvocation(methodName, superInvocationName);
		}
	}

}
