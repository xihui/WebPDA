/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.webpda.server.core.datainterface;

import java.util.LinkedHashMap;

/**
 * The abstract factory that creates specific PV.
 * @author           Xihui Chen
 */
public abstract class AbstractPVFactory {		

	
	/**Create a PV.
	 * @param name name of the PV. Must not be null.
	 * @param parameters the parameters in a linked hash map, which could be directly converted from a JSON object.
	 * Could be null.
	 * @return the PV.
	 * @throws Exception error on creating pv.
	 */
	public abstract IPV createPV(final String name, LinkedHashMap<String, Object> parameters) throws Exception;
	
	

}
