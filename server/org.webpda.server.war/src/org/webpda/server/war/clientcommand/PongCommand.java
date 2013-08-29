/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

/**Pong command from client.
 * @author Xihui Chen
 *
 */
public class PongCommand extends AbstractClientCommand {

	private long count =0;
	
	@Override
	public void run() {
		getClientSession().setPongCount(count);
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(long count) {
		this.count = count;
	}

}
