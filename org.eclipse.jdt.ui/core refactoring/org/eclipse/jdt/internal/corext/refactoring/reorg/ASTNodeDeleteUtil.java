/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;

public class ASTNodeDeleteUtil {

	private static ASTNode[] getNodesToDelete(IJavaElement element, CompilationUnit cuNode) throws JavaModelException {
		if (element.getElementType() == IJavaElement.FIELD)
			return new ASTNode[] { ASTNodeSearchUtil.getFieldDeclarationFragmentNode((IField) element, cuNode)};
		if (element.getElementType() == IJavaElement.TYPE && ((IType) element).isLocal()) {
			IType type= (IType) element;
			if (type.isAnonymous()) {
				return new ASTNode[] { ASTNodeSearchUtil.getClassInstanceCreationNode(type, cuNode)};
			} else {
				ASTNode[] nodes= ASTNodeSearchUtil.getDeclarationNodes(element, cuNode);
				nodes[0]= nodes[0].getParent();
				return nodes;
			}
		}
		return ASTNodeSearchUtil.getDeclarationNodes(element, cuNode);
	}

	private static Set getRemovedNodes(final List removed, final CompilationUnitRewrite rewrite) {
		final Set result= new HashSet();
		rewrite.getRoot().accept(new GenericVisitor(true) {

			protected boolean visitNode(ASTNode node) {
				if (removed.contains(node))
					result.add(node);
				return true;
			}
		});
		return result;
	}

	public static void markAsDeleted(IJavaElement[] javaElements, CompilationUnitRewrite rewrite, TextEditGroup group) throws JavaModelException {
		final List removed= new ArrayList();
		for (int i= 0; i < javaElements.length; i++) {
			markAsDeleted(removed, javaElements[i], rewrite, group);
		}
		propagateFieldDeclarationNodeDeletions(removed, rewrite, group);
	}

	private static void markAsDeleted(List list, IJavaElement element, CompilationUnitRewrite rewrite, TextEditGroup group) throws JavaModelException {
		ASTNode[] declarationNodes= getNodesToDelete(element, rewrite.getRoot());
		for (int i= 0; i < declarationNodes.length; i++) {
			ASTNode node= declarationNodes[i];
			if (node != null) {
				list.add(node);
				rewrite.getASTRewrite().remove(node, group);
				rewrite.getImportRemover().registerRemovedNode(node);
			}
		}
	}

	private static void propagateFieldDeclarationNodeDeletions(final List removed, final CompilationUnitRewrite rewrite, final TextEditGroup group) {
		Set removedNodes= getRemovedNodes(removed, rewrite);
		for (Iterator iter= removedNodes.iterator(); iter.hasNext();) {
			ASTNode node= (ASTNode) iter.next();
			if (node instanceof VariableDeclarationFragment) {
				if (node.getParent() instanceof FieldDeclaration) {
					FieldDeclaration fd= (FieldDeclaration) node.getParent();
					if (!removed.contains(fd) && removedNodes.containsAll(fd.fragments()))
						rewrite.getASTRewrite().remove(fd, group);
					rewrite.getImportRemover().registerRemovedNode(fd);
				}
			}
		}
	}

	private ASTNodeDeleteUtil() {
	}
}