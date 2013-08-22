/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

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
	
}
