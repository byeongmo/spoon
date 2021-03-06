/* 
 * Spoon - http://spoon.gforge.inria.fr/
 * Copyright (C) 2006 INRIA Futurs <renaud.pawlak@inria.fr>
 * 
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify 
 * and/or redistribute the software under the terms of the CeCILL-C license as 
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *  
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package spoon.reflect.visitor;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtAnnotationFieldAccess;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssert;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.code.CtTargetedAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtAnnotationType;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtCodeSnippet;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtGenericElement;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtMultiTypedElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtCatchVariableReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtGenericElementReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeAnnotableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtUnboundVariableReference;
import spoon.reflect.reference.CtVariableReference;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * This class provides an abstract implementation of the visitor that allows its
 * subclasses to scans the metamodel elements by recursively using their
 * (abstract) supertype scanning methods.
 */
public abstract class CtInheritanceScanner implements CtVisitor {

	/**
	 * Default constructor.
	 */
	public CtInheritanceScanner() {
	}

	public <T> void visitCtCodeSnippetExpression(
			CtCodeSnippetExpression<T> e) {
		scanCtCodeSnippet(e);
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtCodeSnippetStatement(CtCodeSnippetStatement e) {
		scanCtCodeSnippet(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	/**
	 * Generically scans a collection of meta-model elements.
	 */
	public void scan(Collection<? extends CtElement> elements) {
		if (elements != null) {
			for (CtElement e : elements) {
				scan(e);
			}
		}
	}

	/**
	 * Generically scans a meta-model element.
	 */
	public void scan(CtElement element) {
		if (element != null) {
			element.accept(this);
		}
	}

	/**
	 * Generically scans a meta-model element reference.
	 */
	public void scan(CtReference reference) {
		if (reference != null) {
			reference.accept(this);
		}
	}

	/**
	 * Scans an abstract invocation.
	 */
	public <T> void scanCtAbstractInvocation(CtAbstractInvocation<T> a) {
	}

	/**
	 * Scans an abstract control flow break.
	 */
	public void scanCtCFlowBreak(CtCFlowBreak flowBreak) {
	}

	/**
	 * Scans an abstract code element.
	 */
	public void scanCtCodeElement(CtCodeElement e) {

	}

	public void scanCtTypeMember(CtTypeMember e) {
	}

	/**
	 * Scans an abstract element.
	 */
	public void scanCtElement(CtElement e) {
	}

	/**
	 * Scans an abstract executable.
	 */
	public <R> void scanCtExecutable(CtExecutable<R> e) {
	}

	/**
	 * Scans an abstract expression.
	 */
	public <T> void scanCtExpression(CtExpression<T> expression) {
	}

	/**
	 * Scans an abstract generic element.
	 */
	public void scanCtGenericElement(CtGenericElement e) {

	}

	public void scanCtVisitable(CtVisitable e) {

	}

	/**
	 * Scans an abstract generic element reference.
	 */
	public void scanCtGenericElementReference(
			CtGenericElementReference reference) {
	}

	/**
	 * Scans an abstract loop.
	 */
	public void scanCtLoop(CtLoop loop) {

	}

	/**
	 * Scans an abstract modifiable element.
	 */
	public void scanCtModifiable(CtModifiable m) {

	}

	/**
	 * Scans an abstract named element.
	 */
	public void scanCtNamedElement(CtNamedElement e) {
	}

	/**
	 * Scans an abstract reference.
	 */
	public void scanCtReference(CtReference reference) {

	}

	/**
	 * Scans an abstract statement.
	 */
	public void scanCtStatement(CtStatement s) {
	}

	/**
	 * Scans an abstract targeted expression.
	 */
	public <T, E extends CtExpression<?>> void scanCtTargetedExpression(
			CtTargetedExpression<T, E> targetedExpression) {
	}

	/**
	 * Scans an abstract type.
	 */
	public <T> void scanCtType(CtType<T> type) {
	}

	/**
	 * Scans an abstract typed element.
	 */
	public <T> void scanCtTypedElement(CtTypedElement<T> e) {
	}

	/**
	 * Scans a type annotation.
	 */
	public void scanCtTypeAnnotableReference(CtTypeAnnotableReference e) {
	}

	/**
	 * Scans an abstract variable declaration.
	 */
	public <T> void scanCtVariable(CtVariable<T> v) {
	}

	public <T> void visitCtFieldAccess(CtFieldAccess<T> f) {
		visitCtVariableAccess(f);
		scanCtTargetedAccess(f);
		scanCtTargetedExpression(f);
	}

	public <T> void visitCtSuperAccess(CtSuperAccess<T> f) {
		scanCtTargetedAccess(f);
		visitCtVariableAccess(f);
		scanCtTargetedExpression(f);
	}

	public void scanCtMultiTypedElement(CtMultiTypedElement f) {
	}

	public <T, A extends T> void visitCtOperatorAssignment(
			CtOperatorAssignment<T, A> e) {
		visitCtAssignment(e);
	}

	/**
	 * Scans an abstract variable reference.
	 */
	public <T> void scanCtVariableReference(CtVariableReference<T> reference) {
	}

	/**
	 * Scans an abstract variable reference.
	 */
	public <T> void scanCtTypeInformation(CtTypeInformation typeInfo) {
	}

	public <A extends Annotation> void visitCtAnnotation(
			CtAnnotation<A> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <A extends Annotation> void visitCtAnnotationType(
			CtAnnotationType<A> e) {
		scanCtType(e);
		scanCtNamedElement(e);
				scanCtTypeInformation(e);
		scanCtTypeMember(e);
		scanCtGenericElement(e);
		scanCtModifiable(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtAnonymousExecutable(CtAnonymousExecutable e) {
		scanCtElement(e);
		scanCtModifiable(e);
		scanCtTypeMember(e);
		scanCtVisitable(e);

	}

	public <T, E extends CtExpression<?>> void visitCtArrayAccess(
			CtArrayAccess<T, E> e) {
		scanCtTargetedExpression(e);
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> e) {
		visitCtTypeReference(e);
	}

	public <T> void visitCtAssert(CtAssert<T> e) {
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T, A extends T> void visitCtAssignment(
			CtAssignment<T, A> e) {
		scanCtStatement(e);
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtBinaryOperator(CtBinaryOperator<T> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <R> void visitCtBlock(CtBlock<R> e) {
		scanCtStatement(e);
		visitCtStatementList(e);
	}

	public void visitCtBreak(CtBreak e) {
		scanCtCFlowBreak(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <E> void visitCtCase(CtCase<E> e) {
		scanCtStatement(e);
		visitCtStatementList(e);
	}

	public void visitCtCatch(CtCatch e) {
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtClass(CtClass<T> e) {
		scanCtType(e);
		scanCtStatement(e);
		scanCtTypeInformation(e);
		scanCtGenericElement(e);
		scanCtCodeElement(e);
		scanCtNamedElement(e);
		scanCtTypeMember(e);
		scanCtElement(e);
		scanCtModifiable(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtConditional(CtConditional<T> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtConstructor(CtConstructor<T> e) {
		scanCtExecutable(e);
		scanCtNamedElement(e);
		scanCtGenericElement(e);
		scanCtTypedElement(e);
		scanCtTypeMember(e);
		scanCtModifiable(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtContinue(CtContinue e) {
		scanCtCFlowBreak(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtDo(CtDo e) {
		scanCtLoop(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T extends Enum<?>> void visitCtEnum(CtEnum<T> e) {
		visitCtClass(e);
	}

	public <T> void visitCtExecutableReference(
			CtExecutableReference<T> e) {
		scanCtReference(e);
		scanCtGenericElementReference(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtField(CtField<T> e) {
		scanCtNamedElement(e);
		scanCtVariable(e);
		scanCtTypeMember(e);
		scanCtModifiable(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void scanCtTargetedAccess(CtTargetedAccess<T> e) {
	}

	public <T> void visitCtThisAccess(CtThisAccess<T> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtFieldReference(CtFieldReference<T> e) {
		scanCtVariableReference(e);
		scanCtReference(e);
		scanCtVisitable(e);
	}

	public void visitCtFor(CtFor e) {
		scanCtLoop(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtForEach(CtForEach e) {
		scanCtLoop(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtIf(CtIf e) {
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtInterface(CtInterface<T> e) {
		scanCtType(e);
		scanCtTypeInformation(e);
		scanCtGenericElement(e);
		scanCtNamedElement(e);
		scanCtTypeMember(e);
		scanCtElement(e);
		scanCtModifiable(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtInvocation(CtInvocation<T> e) {
		scanCtAbstractInvocation(e);
		scanCtStatement(e);
		scanCtTargetedExpression(e);
		scanCtElement(e);
		scanCtCodeElement(e);
		scanCtExpression(e);
		scanCtVisitable(e);
		scanCtTypedElement(e);
	}

	public <T> void visitCtLiteral(CtLiteral<T> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtLocalVariable(CtLocalVariable<T> e) {
		scanCtStatement(e);
		scanCtVariable(e);
		scanCtCodeElement(e);
		scanCtNamedElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtModifiable(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtLocalVariableReference(
			CtLocalVariableReference<T> e) {
		scanCtVariableReference(e);
		scanCtReference(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtCatchVariable(CtCatchVariable<T> e) {
		scanCtVariable(e);
		scanCtMultiTypedElement(e);
		scanCtCodeElement(e);
		scanCtNamedElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtModifiable(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtCatchVariableReference(CtCatchVariableReference<T> e) {
		scanCtVariableReference(e);
		scanCtReference(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtMethod(CtMethod<T> e) {
		scanCtExecutable(e);
		scanCtTypedElement(e);
		scanCtNamedElement(e);
		scanCtGenericElement(e);
		scanCtTypeMember(e);
		scanCtElement(e);
		scanCtModifiable(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtNewArray(CtNewArray<T> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	@Override
	public <T> void visitCtConstructorCall(CtConstructorCall<T> e) {
		scanCtTargetedExpression(e);
		scanCtAbstractInvocation(e);
		scanCtStatement(e);
		scanCtExpression(e);
		scanCtElement(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtNewClass(CtNewClass<T> e) {
		visitCtConstructorCall(e);
	}

	@Override
	public <T> void visitCtLambda(CtLambda<T> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtExecutable(e);
		scanCtNamedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	@Override
	public <T, E extends CtExpression<?>> void visitCtExecutableReferenceExpression(CtExecutableReferenceExpression<T, E> e) {
		scanCtTargetedExpression(e);
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T, A extends T> void visitCtOperatorAssignement(
			CtOperatorAssignment<T, A> assignment) {
	}

	public void visitCtPackage(CtPackage e) {
		scanCtNamedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtPackageReference(CtPackageReference e) {
		scanCtReference(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtParameter(CtParameter<T> e) {
		scanCtNamedElement(e);
		scanCtVariable(e);
		scanCtModifiable(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtParameterReference(CtParameterReference<T> e) {
		scanCtVariableReference(e);
		scanCtReference(e);
		scanCtVisitable(e);
	}

	public <R> void visitCtReturn(CtReturn<R> e) {
		scanCtCFlowBreak(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <R> void visitCtStatementList(CtStatementList e) {
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <E> void visitCtSwitch(CtSwitch<E> e) {
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtSynchronized(CtSynchronized e) {
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtThrow(CtThrow e) {
		scanCtCFlowBreak(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtTry(CtTry e) {
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	@Override
	public void visitCtTryWithResource(CtTryWithResource e) {
		visitCtTry(e);
	}

	public void visitCtTypeParameter(CtTypeParameter e) {
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public void visitCtTypeParameterReference(CtTypeParameterReference e) {
		visitCtTypeReference(e);
	}

	public <T> void visitCtTypeReference(CtTypeReference<T> e) {
		scanCtReference(e);
		scanCtTypeInformation(e);
		scanCtGenericElementReference(e);
		scanCtTypeAnnotableReference(e);
		scanCtVisitable(e);
	}

	@Override
	public <T> void visitCtTypeAccess(CtTypeAccess<T> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtUnaryOperator(CtUnaryOperator<T> e) {
		scanCtExpression(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtVariableAccess(CtVariableAccess<T> e) {
		scanCtExpression(e);
		scanCtCodeElement(e);
		scanCtTypedElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtAnnotationFieldAccess(
			CtAnnotationFieldAccess<T> e) {
		visitCtVariableAccess(e);
		scanCtTargetedExpression(e);
	}

	public void visitCtWhile(CtWhile e) {
		scanCtLoop(e);
		scanCtStatement(e);
		scanCtCodeElement(e);
		scanCtElement(e);
		scanCtVisitable(e);
	}

	public <T> void visitCtUnboundVariableReference(
			CtUnboundVariableReference<T> reference) {

	}

	public void scanCtCodeSnippet(CtCodeSnippet snippet) {
	}
}
