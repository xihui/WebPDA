/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.Session;

import org.webpda.server.core.IPeer;
import org.webpda.server.core.servermessage.IServerMessage;

/**Implementation of IPeer using JSR356 session.
 * @author Xihui Chen
 *
 */
public class JSR356Peer implements IPeer{

	private Session session;	
	

	public JSR356Peer(Session session) {
		this.session = session;
	}

	@Override
	public void close() throws IOException {
		session.close();
	}

	@Override
	public boolean isOpen() {
		return session.isOpen();
	}

	@Override
	public void sendBinary(ByteBuffer byteBuffer) throws Exception {
		session.getBasicRemote().sendBinary(byteBuffer);
	}

	@Override
	public void sendObject(IServerMessage message) throws Exception {
		session.getBasicRemote().sendObject(message);
	}

	@Override
	public String getId() {
		return session.getId();
	}
	
	
}
