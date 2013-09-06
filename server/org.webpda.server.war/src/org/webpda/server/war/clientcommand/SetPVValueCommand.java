/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

/**Client command to set pv value.
 * @author Xihui Chen
 *
 */
public class SetPVValueCommand extends AbstractPVCommand {

	public static final String AUTHORIZATION_KEY = "SetPVValue";
	private Object value;
	
	@Override
	public void run() {
		getClientSession().getPV(getId()).setValue(value);		
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public String getAuthorizationKey() {
		return AUTHORIZATION_KEY;		
	}

}
