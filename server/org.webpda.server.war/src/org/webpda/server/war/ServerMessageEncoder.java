/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.webpda.server.core.servermessage.IServerMessage;

import com.fasterxml.jackson.core.JsonProcessingException;

/**Encode server message to json.
 * @author Xihui Chen
 *
 */
public class ServerMessageEncoder implements Encoder.Text<IServerMessage>{

	@Override
	public void destroy() {
		
	}

	@Override
	public void init(EndpointConfig config) {
		
	}

	@Override
	public String encode(IServerMessage object) throws EncodeException {	
		try {
			return object.toJson();
		} catch (JsonProcessingException e) {
			throw new EncodeException(object, "Failed to encode " + object, e);
		}		
	}

	
	
	
}
