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
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtEnum;
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

	public static StaticCallGraph createCallGraphSpoon(CtModel model) {
		StaticCallGraph graph = new StaticCallGraph();
		
		List<CtClass<?>> allClasses = model.getElements(new TypeFilter<>(CtClass.class));
		
		for(CtClass<?> c : allClasses) {
			
			if(c instanceof CtClass<?> && !(c instanceof CtEnum<?>))
				graph.addClassSpoon(c);
			
			for(CtConstructor<?> cons : c.getConstructors()) {
				graph.addConstructorAndInvocationsSpoon(c, cons);
			}
			
			for(CtMethod<?> m : c.getMethods()) {
				graph.addInvocationsSpoon(c, c.getQualifiedName()+"::"+m.getSimpleName(), m.getElements(new TypeFilter<>(CtInvocation.class)));
			}
			
		}
		
		return graph;
		
	}

	public boolean addMethodAndInvocationsSpoon(CtType<?> cls, CtMethod<?> method) {

		if (method.getBody() != null) {
			String methodName = cls.getReference().getQualifiedName() + "::" + method.getSimpleName();

			this.addNode(methodName);
			List<CtInvocation<?>> invocationCollector = method.getElements(new TypeFilter<>(CtInvocation.class));
			this.addInvocationsSpoon(cls, methodName, invocationCollector);

		}
		return method.getBody() != null;

	}
	
	private boolean addConstructorAndInvocationsSpoon(CtClass<?> cls, CtConstructor<?> cons) {
		if (cons.getBody() != null && cons.getElements(new TypeFilter<>(CtInvocation.class)).size()>1) {
			
			String constructorName = cls.getReference().getQualifiedName() + "::" + cons.getDeclaringType().getSimpleName();
			this.addNode(constructorName);
			List<CtInvocation<?>> invocationCollector = cons.getElements(new TypeFilter<>(CtInvocation.class));
			this.addInvocationsSpoon(cls, constructorName, invocationCollector);

		}
		return cons.getBody() != null;
		
	}

	private void addInvocationsSpoon(CtType<?> cls, String methodName,
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