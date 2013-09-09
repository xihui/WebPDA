/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.webpda.server.core.servermessage.IServerMessage;

/**The interface that defines a client peer.
 * @author Xihui Chen
 *
 */
public interface IPeer {

	public void close() throws IOException;

	public boolean isOpen();

	public void sendBinary(ByteBuffer byteBuffer) throws Exception;

	public void sendObject(IServerMessage message) throws Exception;

	public String getId();
	
	/**
	 * Set max buffer size in bytes.
	 */
	public void setMaxBufferSize(int bytes);

}
