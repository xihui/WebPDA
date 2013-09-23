/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.datainterface.cs.pvmanager;

import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.webpda.server.core.datainterface.AbstractPVFactory;
import org.webpda.server.core.datainterface.ExceptionHandler;
import org.webpda.server.core.datainterface.IPV;
import org.webpda.server.core.util.LoggerUtil;

/**A simple pv factory that creates {@link PVManagerPV}.
 * @author Xihui Chen
 *
 */
public class PVManagerPVFactory extends AbstractPVFactory {	
	
	public static final String READ_ONLY = "readOnly";
	public static final String UPDATE_PERIOD = "minUpdatePeriodInMs";
	public static final String BUFFER_ALL_VALUES = "bufferAllValues";
	
	/**
	 * The default background thread for PV change event notification. It will only be created 
	 * on its first use.
	 */
	static ExecutorService SIMPLE_PV_THREAD = Executors.newSingleThreadExecutor();
	
	private static ExceptionHandler DEFAULT_EXCEPTION_HANDLER = new ExceptionHandler() {
		
		@Override
		public void handleException(Exception exception) {
			//TODO: Glassfish has a but that following line could make logging system fail.
//			LoggerUtil.getLogger().log(Level.FINE, "Exception from PV", exception);
		}
	};
	
	
	public PVManagerPVFactory() {
	}
	
	@Override
	public IPV createPV(String name, LinkedHashMap<String , Object> parameters) {
		boolean readOnly = false;
		long minUpdatePeriodInMs = 10;
		boolean bufferAllValues = false;
		if(parameters !=null){
			if (parameters.containsKey(READ_ONLY))
				readOnly = (Boolean) parameters.get(READ_ONLY);
			if (parameters.containsKey(UPDATE_PERIOD))
				minUpdatePeriodInMs = (Integer) parameters.get(UPDATE_PERIOD);
			if (parameters.containsKey(BUFFER_ALL_VALUES))
				bufferAllValues = (Boolean) parameters.get(BUFFER_ALL_VALUES);
		}
		PVManagerPV.setDebug(true);
		return new PVManagerPV(name, readOnly, minUpdatePeriodInMs,
				bufferAllValues, SIMPLE_PV_THREAD, DEFAULT_EXCEPTION_HANDLER);
	}
	
	public static synchronized ExecutorService getDefaultPVNotificationThread() {
		if (SIMPLE_PV_THREAD == null)
			SIMPLE_PV_THREAD = Executors.newSingleThreadExecutor();
		return SIMPLE_PV_THREAD;
	}

	
}
