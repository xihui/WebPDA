/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

/**Client command to stop a pv. It has all the same parameters as {@link CreatePVCommand}, so
 * it simply extends it.
 * @author Xihui Chen
 *
 */
public class StopPVCommand extends CreatePVCommand {

	@Override
	public void run() {
		getClientSession().removePV(this);		
	}

}
