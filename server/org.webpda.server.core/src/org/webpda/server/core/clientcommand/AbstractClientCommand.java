/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.clientcommand;

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
	
	public boolean isPermitted(){
		if(getAuthorizationKey() !=null){
			return clientSession.hasPermission(getAuthorizationKey());
		}
		return true;
	}
	
	/**Get the authorization key.If it is null, it means this 
	 * command doesn't need authorization. In convention, the key should
	 * be same as the commandName.
	 * @return authorization key of the this command. 
	 */
	public String getAuthorizationKey(){
		return null;
	}

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
