/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core;

/**Listener that will be notified on each heart beat.
 * @author Xihui Chen
 *
 */
public interface HeartBeatListener {	
	
	public void beat(long heartBeatCount);
	
	/**The listener should be notified on every this number of heart beats.
	 * @return The interval of heart beats that this listener should be notified.
	 */
	public int getNotifyInterval();

}
