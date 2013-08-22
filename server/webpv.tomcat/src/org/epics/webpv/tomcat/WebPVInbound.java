/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.epics.webpv.tomcat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.MessageInbound;
import org.epics.webpv.core.IWSMessageWriter;
import org.epics.webpv.core.WSClient;

/**
 * @author Xihui Chen
 *
 */
public class WebPVInbound extends MessageInbound {

	private WSClient wsClient;
	
	public WebPVInbound(int byteBufferMaxSize, int charBufferMaxSize) {
		super();
        setByteBufferMaxSize(byteBufferMaxSize);
        setCharBufferMaxSize(charBufferMaxSize);
        wsClient = new WSClient();
	}
	
	protected void onOpen(org.apache.catalina.websocket.WsOutbound outbound) {		
		super.onOpen(outbound);
		wsClient.onOpen(new TomcatMessageWriter(outbound));
		
	};

	@Override
	protected void onTextMessage(CharBuffer message) throws IOException {
		
	}

	@Override
	protected void onBinaryMessage(ByteBuffer message) throws IOException {
		
	}

}
