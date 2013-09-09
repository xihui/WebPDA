/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.clientcommand;

/**Set max buffer size for this client.
 * @author Xihui Chen
 *
 */
public class SetServerBufferSizeCommand extends AbstractClientCommand {

	private int size =0;
	
	@Override
	public void run() {
		if(size > ClientSession.MAX_BUFFER_SIZE)
			size = ClientSession.MAX_BUFFER_SIZE;
		getClientSession().setMaxQueueBytes(size);
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	@Override
	public String getAuthorizationKey() {
		return CreatePVCommand.AUTHORIZATION_KEY;
	}
}
