/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

import java.util.logging.Level;

import javax.websocket.Session;

import org.webpda.server.core.LoggerUtil;
import org.webpda.server.war.servermessage.IServerMessage;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**The command sent from client. Server needs to execute the command.
 * @author Xihui Chen
 *
 */
public abstract class AbstractClientCommand {
	
	private String commandName;
	
	@JsonIgnore
	private ClientSession clientSession;

	public abstract void run();

	public ClientSession getClientSession() {
		return clientSession;
	}

	public void setClientSession(ClientSession clientSession) {
		this.clientSession = clientSession;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
	
	@Override
	public String toString() {
		return commandName;
	}

	/**Send message to client.
	 * @param message the message.
	 */
	protected void send(IServerMessage message) {
		final Session session = clientSession.getSession();
		if(session.isOpen()){
			try {
				session.getBasicRemote().sendObject(message);
			} catch (Exception e) {
				LoggerUtil.getLogger().log(Level.SEVERE, "Send server message error.", e);
			} 
		}else
			getClientSession().close();
	}
	
}
