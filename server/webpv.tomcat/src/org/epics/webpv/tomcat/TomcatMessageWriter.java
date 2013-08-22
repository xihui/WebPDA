/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.epics.webpv.tomcat;

import java.io.IOException;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.WsOutbound;
import org.epics.webpv.core.IWSMessageWriter;

/**
 * @author Xihui Chen
 *
 */
public class TomcatMessageWriter implements IWSMessageWriter{

	private WsOutbound wsOutbound;
	
	
	
	public TomcatMessageWriter(WsOutbound wsOutbound) {
		this.wsOutbound = wsOutbound;
	}

	@Override
	public void writeString(String text) throws IOException {
		wsOutbound.writeTextMessage(CharBuffer.wrap(text));
	}

}
