/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.datainterface.controlsystem.pvmanager;

import java.util.concurrent.Executor;

import org.webpda.server.datainterface.AbstractPVFactory;
import org.webpda.server.datainterface.ExceptionHandler;
import org.webpda.server.datainterface.IPV;

/**A simple pv factory that creates {@link PVManagerPV}.
 * @author Xihui Chen
 *
 */
public class PVManagerPVFactory extends AbstractPVFactory {

	
	
	public PVManagerPVFactory() {
	}
	
	@Override
	public IPV createPV(String name, boolean readOnly, long minUpdatePeriod, boolean bufferAllValues,
			Executor notificationThread, ExceptionHandler exceptionHandler) {
		return new PVManagerPV(name, readOnly, minUpdatePeriod, bufferAllValues, notificationThread, exceptionHandler);
	}

	
}
