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
