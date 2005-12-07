/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.refactoring;

import org.eclipse.jdt.core.IJavaElement;

/**
 * An <code>IJavaElementMapper</code> provides methods to map an original
 * elements to its refactored counterparts.
 * 
 * @since 3.2
 */
public interface IJavaElementMapper {
	
	/**
	 * Returns the refactored Java element for the given element.
	 * The returned Java element might not yet exist when the method 
	 * is called.
	 * </p>
	 * Note that local variables <strong>cannot</strong> be mapped 
	 * using this method.
	 * <p>
	 * 
	 * @param element the element to be refactored
	 * 
	 * @return the refactored element for the given element
	 */
	IJavaElement getRefactoredJavaElement(IJavaElement element);
}