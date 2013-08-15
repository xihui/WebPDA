/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

/**Abstract client command dealing with a single pv.
 * @author Xihui Chen
 *
 */
public abstract class AbstractPVCommand extends AbstractClientCommand{
	
	protected String pvName;
	protected int id;

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof AbstractPVCommand){
			if(pvName.equals(((AbstractPVCommand)obj).getPvName())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return pvName.hashCode();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
