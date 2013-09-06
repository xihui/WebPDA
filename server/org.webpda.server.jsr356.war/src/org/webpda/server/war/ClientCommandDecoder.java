/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war;
import java.util.logging.Level;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.webpda.server.core.clientcommand.AbstractClientCommand;
import org.webpda.server.core.util.JsonUtil;
import org.webpda.server.core.util.LoggerUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**Decoder json message received from client to an {@link AbstractClientCommand}.
 * @author Xihui Chen
 *
 */
public class ClientCommandDecoder implements Decoder.Text<AbstractClientCommand>{

	private String packageName = AbstractClientCommand.class.getPackage().getName();;

	@Override
	public void destroy() {
		
	}

	@Override
	public void init(EndpointConfig config) {
		
	}

	@Override
	public AbstractClientCommand decode(String arg0) throws DecodeException {
		try {
			JsonNode rootNode = JsonUtil.mapper.readValue(arg0, JsonNode.class);
			String commandName = rootNode.get("commandName").asText();
			Class<?> clazz = Class.forName(
					packageName + "." + commandName + "Command");
			AbstractClientCommand command = 
					(AbstractClientCommand) JsonUtil.mapper.readValue(arg0, clazz);
			return command;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Error in decoding command.", e);
		
		} 
		return null;
	}

	@Override
	public boolean willDecode(String arg0) {
		JsonNode rootNode;
		try {
			rootNode = JsonUtil.mapper.readValue(arg0, JsonNode.class);
			String commandName = rootNode.get("commandName").asText();		
			Class<?> clazz = Class.forName(packageName + "." + commandName + "Command");
			if(clazz != null)
				return true;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Error in decoding command.", e);
		} 
		return false;
	}

	

}
