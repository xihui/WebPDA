/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.servermessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.webpda.server.core.JsonUtil;

import com.fasterxml.jackson.core.JsonGenerator;


/**A server message that is not a PV event. It has a message field to indicate 
 * the message name.
 * @author Xihui Chen
 *
 */
public abstract class NonPVEventMessage implements IServerMessage{



	private static final String MESSAGE = "msg";

	protected JsonGenerator createJsonGenerator() throws IOException{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		JsonGenerator jg = JsonUtil.jsonFactory.createGenerator(outputStream);
		jg.writeStartObject();
		jg.writeStringField(MESSAGE, getClass().getSimpleName().replaceAll("Message", ""));
		return jg;
	}
	
	
}
