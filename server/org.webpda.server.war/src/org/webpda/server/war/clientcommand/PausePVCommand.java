/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

/**Client command to pause/resume a pv.
 * @author Xihui Chen
 *
 */
public class PausePVCommand extends AbstractPVCommand {

	private boolean paused = false;
	
	@Override
	public void run() {
		getClientSession().getPV(getId()).setPaused(paused);		
	}

	/**
	 * @return true if the pv is paused.
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * @param paused the pause to set
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
	}

}
